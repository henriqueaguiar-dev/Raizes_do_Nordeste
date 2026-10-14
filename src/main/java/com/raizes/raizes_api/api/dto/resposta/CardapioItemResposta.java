package com.raizes.raizes_api.api.dto.resposta;

import java.math.BigDecimal;
import java.util.UUID;

public record CardapioItemResposta(UUID produtoId, String nome, String descricao,
                                  BigDecimal preco, boolean disponivel) {}
