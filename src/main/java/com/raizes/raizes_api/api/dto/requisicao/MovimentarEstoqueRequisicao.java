package com.raizes.raizes_api.api.dto.requisicao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class MovimentarEstoqueRequisicao {

    @NotNull(message = "A unidade e obrigatoria.")
    private UUID unidadeId;

    @NotNull(message = "O produto e obrigatorio.")
    private UUID produtoId;

    @NotNull(message = "A quantidade e obrigatoria.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    private Integer quantidade;

    public UUID getUnidadeId() {
        return unidadeId;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }
}
