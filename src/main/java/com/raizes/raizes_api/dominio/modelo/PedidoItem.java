package com.raizes.raizes_api.dominio.modelo;

import java.math.BigDecimal;
import java.util.UUID;

public class PedidoItem {

    private UUID id;
    private UUID produtoId;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subTotal;

    public PedidoItem() {
    }

    public PedidoItem(UUID id, UUID produtoId, Integer quantidade, BigDecimal precoUnitario, BigDecimal subTotal) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subTotal = subTotal;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

}
