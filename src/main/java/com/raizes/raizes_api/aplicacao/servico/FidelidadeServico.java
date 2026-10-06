package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.resposta.FidelidadeSaldoResposta;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PagamentoJpaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class FidelidadeServico {
    private final PagamentoJpaRepositorio pagamentoRepositorio;

    public FidelidadeServico(PagamentoJpaRepositorio pagamentoRepositorio) {
        this.pagamentoRepositorio = pagamentoRepositorio;
    }

    @Transactional(readOnly = true)
    public FidelidadeSaldoResposta consultarSaldo(UUID clienteId) {
        BigDecimal totalPago = pagamentoRepositorio.somarValorPorClienteEStatus(
                clienteId, StatusPagamento.APROVADO);
        long pontos = totalPago == null ? 0 : totalPago.setScale(0, RoundingMode.DOWN).longValueExact();
        return new FidelidadeSaldoResposta(clienteId, pontos);
    }
}
