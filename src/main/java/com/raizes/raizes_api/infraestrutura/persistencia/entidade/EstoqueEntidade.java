package com.raizes.raizes_api.infraestrutura.persistencia.entidade;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity 
@Table(name = "estoque",
    uniqueConstraints = {@UniqueConstraint(name = "uk_estoque_unidade_produto", columnNames = { "unidade_id", "produto_id" })})
public class EstoqueEntidade {

    @Id 
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(name = "produto_id", nullable = false)
    private UUID produtoId;

    @Column(name = "quantidade_disponivel", nullable = false)
    private Integer quantidadeDisponivel;

    public EstoqueEntidade() {
    }

    public EstoqueEntidade(UUID id, UUID unidadeId, UUID produtoId, Integer quantidadeDisponivel) {
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
