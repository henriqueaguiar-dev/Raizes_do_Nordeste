package com.raizes.raizes_api.api.dto.resposta;

import java.math.BigDecimal;
import java.util.UUID;

public class ProdutoResposta {

    private UUID id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private boolean ativo;

    public ProdutoResposta(UUID id, String nome, String descricao, BigDecimal preco, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.ativo = ativo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public boolean isAtivo() {
        return ativo;
    }
}