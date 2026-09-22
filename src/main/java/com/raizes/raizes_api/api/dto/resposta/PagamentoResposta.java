package com.raizes.raizes_api.api.dto.resposta;

import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;

import java.math.BigDecimal;
import java.util.UUID;

public class PagamentoResposta {

    private UUID id;
    private UUID pedidoId;
    private StatusPagamento status;
    private MetodoPagamento metodo;
    private BigDecimal valor;
    private String codigoTransacaoMock;
    private String respostaMock;

    public PagamentoResposta(UUID id, UUID pedidoId, StatusPagamento status, MetodoPagamento metodo,
                             BigDecimal valor, String codigoTransacaoMock, String respostaMock) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.status = status;
        this.metodo = metodo;
        this.valor = valor;
        this.codigoTransacaoMock = codigoTransacaoMock;
        this.respostaMock = respostaMock;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getCodigoTransacaoMock() {
        return codigoTransacaoMock;
    }

    public String getRespostaMock() {
        return respostaMock;
    }
}
