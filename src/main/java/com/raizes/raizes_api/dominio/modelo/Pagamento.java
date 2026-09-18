package com.raizes.raizes_api.dominio.modelo;

import java.math.BigDecimal;
import java.util.UUID;

import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;

public class Pagamento {

    private UUID id;
    private UUID pedidoId;
    private StatusPagamento status;
    private MetodoPagamento metodo;
    private BigDecimal valor;
    private String codigoTransacaoMock;

    public Pagamento(){
    }

    public Pagamento(UUID id, UUID pedidoId, MetodoPagamento metodo, BigDecimal valor) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.metodo = metodo;
        this.valor = valor;
        this.status = StatusPagamento.PENDENTE;
    }

    public void aprovar(String codigoTransacaoMock) {
        this.status = StatusPagamento.APROVADO;
        this.codigoTransacaoMock = codigoTransacaoMock;
    }

    public void recusar(String codigoTransacaoMock) {
        this.status = StatusPagamento.RECUSADO;
        this.codigoTransacaoMock = codigoTransacaoMock;
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

    

}
