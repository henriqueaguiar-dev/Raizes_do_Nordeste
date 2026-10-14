package com.raizes.raizes_api.api.dto.requisicao;

import jakarta.validation.constraints.NotNull;

public record AtualizarCardapioRequisicao(
        @NotNull(message = "O status ativo no cardapio e obrigatorio.") Boolean ativo) {}
