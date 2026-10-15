package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.resposta.FidelidadeSaldoResposta;
import com.raizes.raizes_api.api.dto.resposta.FidelidadeResgateResposta;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.*;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class FidelidadeServico {
    private static final long CUSTO_CREDITO = 100;
    private final FidelidadeDebitoJpaRepositorio debitos;
    private final UsuarioJpaRepositorio usuarios;
    private final FidelidadeCreditoJpaRepositorio creditos;
    private final FidelidadeResgateJpaRepositorio resgates;

    public FidelidadeServico(UsuarioJpaRepositorio usuarios, FidelidadeCreditoJpaRepositorio creditos,
                             FidelidadeResgateJpaRepositorio resgates, FidelidadeDebitoJpaRepositorio debitos) {
        this.debitos = debitos;
        this.usuarios = usuarios;
        this.creditos = creditos;
        this.resgates = resgates;
    }

    @Transactional
    public void registrarCredito(UUID clienteId, PagamentoEntidade pagamento) {
        if (pagamento.getStatus() != StatusPagamento.APROVADO) return;
        UsuarioEntidade usuario = buscarComBloqueio(clienteId);
        if (!usuario.isAtivo() || !usuario.isConsentimentoLgpd()) return;
        if (!creditos.existsByPagamentoId(pagamento.getId())) {
            creditos.save(new FidelidadeCreditoEntidade(clienteId, pagamento.getId(), pagamento.getValorExterno()));
            long blocos = calcularSaldo(clienteId) / CUSTO_CREDITO;
            if (blocos > 0) {
                resgates.save(new FidelidadeResgateEntidade(clienteId, blocos * CUSTO_CREDITO,
                        "CREDITO_PAGAMENTO", BigDecimal.valueOf(blocos).setScale(2)));
            }
        }
    }

    @Transactional
    public FidelidadeSaldoResposta consultarSaldo(UUID clienteId) {
        buscarComBloqueio(clienteId);
        long ganhos = calcularPontosGanhos(clienteId);
        long convertidos = resgates.somarPontos(clienteId);
        BigDecimal creditoConvertido = resgates.somarCredito(clienteId);
        return new FidelidadeSaldoResposta(clienteId, ganhos, convertidos,
                creditoConvertido == null ? new BigDecimal("0.00") : creditoConvertido.setScale(2),
                ganhos - convertidos, calcularCredito(clienteId));
    }

    @Transactional
    public FidelidadeResgateResposta resgatar(UUID clienteId) {
        UsuarioEntidade usuario = buscarComBloqueio(clienteId);
        if (!usuario.isAtivo() || !usuario.isConsentimentoLgpd()) {
            throw new RegraDeNegocioExcecao("Resgate exige usuario ativo e consentimento para fidelidade.");
        }
        if (calcularSaldo(clienteId) < CUSTO_CREDITO) {
            throw new RegraDeNegocioExcecao("Saldo insuficiente. O credito de R$ 1,00 exige 100 pontos.");
        }
        return paraResposta(resgates.save(new FidelidadeResgateEntidade(clienteId, CUSTO_CREDITO, "CREDITO_PAGAMENTO", new BigDecimal("1.00"))));
    }

    @Transactional(readOnly = true)
    public List<FidelidadeResgateResposta> listarResgates(UUID clienteId) {
        usuarios.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Usuario nao encontrado."));
        return resgates.findByClienteIdOrderByCriadoEmDesc(clienteId).stream().map(this::paraResposta).toList();
    }

    @Transactional
    public void validarUsoCredito(UUID clienteId, BigDecimal valor) {
        if (valor.signum() == 0) return;
        UsuarioEntidade usuario = buscarComBloqueio(clienteId);
        if (valor.signum() < 0 || !usuario.isAtivo() || !usuario.isConsentimentoLgpd()) {
            throw new RegraDeNegocioExcecao("Uso do credito exige usuario ativo, consentimento e valor positivo.");
        }
        if (calcularCredito(clienteId).compareTo(valor) < 0) {
            throw new RegraDeNegocioExcecao("Saldo de credito de fidelidade insuficiente.");
        }
    }

    @Transactional
    public void debitarCredito(UUID clienteId, PagamentoEntidade pagamento) {
        if (pagamento.getStatus() != StatusPagamento.APROVADO || pagamento.getValorFidelidade().signum() == 0) return;
        validarUsoCredito(clienteId, pagamento.getValorFidelidade());
        debitos.save(new FidelidadeDebitoEntidade(clienteId, pagamento.getId(), pagamento.getValorFidelidade()));
    }

    @Transactional
    public void estornarPagamento(UUID clienteId, UUID pagamentoId) {
        buscarComBloqueio(clienteId);
        creditos.findByPagamentoId(pagamentoId).ifPresent(c -> c.estornar());
        debitos.findByPagamentoId(pagamentoId).ifPresent(d -> d.estornar());
        long pontosValidos=calcularPontosGanhos(clienteId);
        long convertidos=resgates.somarPontos(clienteId);
        if(convertidos>pontosValidos) {
            long reverter=((convertidos-pontosValidos+99)/100)*100;
            resgates.save(new FidelidadeResgateEntidade(clienteId,-reverter,"ESTORNO_CONVERSAO",
                    BigDecimal.valueOf(-reverter/100).setScale(2)));
        }
    }

    private BigDecimal calcularCredito(UUID clienteId) {
        BigDecimal recebido = resgates.somarCredito(clienteId);
        BigDecimal gasto = debitos.somarValor(clienteId);
        return (recebido == null ? BigDecimal.ZERO : recebido)
                .subtract(gasto == null ? BigDecimal.ZERO : gasto).setScale(2);
    }

    private UsuarioEntidade buscarComBloqueio(UUID clienteId) {
        return usuarios.buscarComBloqueio(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Usuario nao encontrado."));
    }

    private long calcularSaldo(UUID clienteId) {
        return calcularPontosGanhos(clienteId) - resgates.somarPontos(clienteId);
    }

    private long calcularPontosGanhos(UUID clienteId) {
        BigDecimal total = creditos.somarValor(clienteId);
        return total == null ? 0 : total.setScale(0, RoundingMode.DOWN).longValueExact();
    }

    private FidelidadeResgateResposta paraResposta(FidelidadeResgateEntidade resgate) {
        return new FidelidadeResgateResposta(resgate.getId(), resgate.getClienteId(), resgate.getPontos(),
                resgate.getBeneficio(), "FID-" + resgate.getId(), resgate.getCriadoEm(), resgate.getValorCredito());
    }
}
