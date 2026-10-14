package com.raizes.raizes_api.api.dto.resposta;

import java.util.UUID;

public record CardapioVinculoResposta(UUID unidadeId, UUID produtoId, boolean ativo) {}
