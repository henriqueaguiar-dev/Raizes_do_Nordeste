package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.ProcessarPagamentoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.PagamentoResposta;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;
import com.raizes.raizes_api.dominio.enums.StatusPedido;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PagamentoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PagamentoJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PedidoJpaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PagamentoServico {

    private final PagamentoJpaRepositorio pagamentoRepositorio;
    private final PedidoJpaRepositorio pedidoRepositorio;
    private final AuditoriaServico auditoriaServico;
    private final FidelidadeServico fidelidadeServico;

    public PagamentoServico(PagamentoJpaRepositorio pagamentoRepositorio,
            PedidoJpaRepositorio pedidoRepositorio,
            AuditoriaServico auditoriaServico, FidelidadeServico fidelidadeServico) {
        this.pagamentoRepositorio = pagamentoRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
        this.auditoriaServico = auditoriaServico;
        this.fidelidadeServico = fidelidadeServico;
    }

    @Transactional
    public PagamentoResposta processar(UUID pedidoId, ProcessarPagamentoRequisicao requisicao, UUID usuarioId, String perfil) {
        PedidoEntidade pedido = pedidoRepositorio.buscarComBloqueio(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Pedido nao encontrado."));

        validarAcesso(pedido, usuarioId, perfil);

        if (pagamentoRepositorio.findByPedidoId(pedidoId).isPresent()) {
            throw new RegraDeNegocioExcecao("Pedido ja possui pagamento registrado.");
        }

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new RegraDeNegocioExcecao("Pedido nao esta aguardando pagamento.");
        }

        java.math.BigDecimal valorFidelidade = requisicao.getValorFidelidade().setScale(2);
        if (valorFidelidade.compareTo(pedido.getTotal()) > 0) {
            throw new RegraDeNegocioExcecao("Credito de fidelidade nao pode exceder o total do pedido.");
        }
        fidelidadeServico.validarUsoCredito(pedido.getClienteId(), valorFidelidade);

        boolean aprovado = Boolean.TRUE.equals(requisicao.getAprovadoMock());

        StatusPagamento statusPagamento = aprovado
                ? StatusPagamento.APROVADO
                : StatusPagamento.RECUSADO;

        String codigoTransacao = "MOCK-" + UUID.randomUUID();

        String respostaMock = aprovado
                ? "Pagamento mock aprovado."
                : "Pagamento mock recusado.";

        PagamentoEntidade pagamento = new PagamentoEntidade(
                UUID.randomUUID(),
                pedido.getId(),
                statusPagamento,
                requisicao.getMetodo(),
                pedido.getTotal(),
                codigoTransacao,
                respostaMock,
                OffsetDateTime.now());

        pagamento.definirValorFidelidade(valorFidelidade);

        PagamentoEntidade pagamentoSalvo = pagamentoRepositorio.save(pagamento);

        auditoriaServico.registrar(
                usuarioId,
                "PROCESSAR_PAGAMENTO",
                "Pagamento",
                pagamentoSalvo.getId(),
                "Pagamento mock com status " + pagamentoSalvo.getStatus());

        if (aprovado) {
            fidelidadeServico.debitarCredito(pedido.getClienteId(), pagamentoSalvo);
            fidelidadeServico.registrarCredito(pedido.getClienteId(), pagamentoSalvo);
            pedido.alterarStatus(StatusPedido.PAGO);
            pedidoRepositorio.save(pedido);
        }

        return paraResposta(pagamentoSalvo);
    }

    public PagamentoResposta buscarPorPedido(UUID pedidoId, UUID usuarioId, String perfil) {
        PedidoEntidade pedido = pedidoRepositorio.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Pedido nao encontrado."));
        validarAcesso(pedido, usuarioId, perfil);
        PagamentoEntidade pagamento = pagamentoRepositorio.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Pagamento nao encontrado para este pedido."));

        return paraResposta(pagamento);
    }

    private void validarAcesso(PedidoEntidade pedido, UUID usuarioId, String perfil) {
        boolean permitido = switch (perfil == null ? "" : perfil) {
            case "CLIENTE" -> pedido.getClienteId().equals(usuarioId);
            case "ADMIN", "GERENTE", "ATENDENTE" -> true;
            default -> false;
        };
        if (!permitido) throw new com.raizes.raizes_api.dominio.excecao.AcessoNegadoExcecao("Voce nao tem permissao para acessar este pagamento.");
    }

    private PagamentoResposta paraResposta(PagamentoEntidade pagamento) {
        PagamentoResposta resposta = new PagamentoResposta(
                pagamento.getId(),
                pagamento.getPedidoId(),
                pagamento.getStatus(),
                pagamento.getMetodo(),
                pagamento.getValor(),
                pagamento.getCodigoTransacaoMock(),
                pagamento.getRespostaMock());
        resposta.definirValorFidelidade(pagamento.getValorFidelidade());
        return resposta;
    }
}
