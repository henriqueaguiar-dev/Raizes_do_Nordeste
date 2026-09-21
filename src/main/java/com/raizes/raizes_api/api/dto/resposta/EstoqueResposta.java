package com.raizes.raizes_api.api.dto.resposta;

import java.util.UUID;

public class EstoqueResposta {

    private UUID id;
    private UUID unidadeId;
    private UUID produtoId;
    private Integer quantidadeDisponivel;

    public EstoqueResposta(UUID id, UUID unidadeId, UUID produtoId, Integer quantidadeDisponivel) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.produtoId = produtoId;
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUnidadeId() {
        return unidadeId;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }
}
