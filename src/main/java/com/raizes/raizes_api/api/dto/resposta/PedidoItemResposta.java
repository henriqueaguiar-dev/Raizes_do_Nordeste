package com.raizes.raizes_api.api.dto.resposta;

import java.math.BigDecimal;
import java.util.UUID;

public class PedidoItemResposta {

    private UUID produtoId;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    public PedidoItemResposta(UUID produtoId, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
