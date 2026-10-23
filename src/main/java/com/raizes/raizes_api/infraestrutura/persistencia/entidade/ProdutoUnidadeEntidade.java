package com.raizes.raizes_api.infraestrutura.persistencia.entidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "produto_unidade", uniqueConstraints = @UniqueConstraint(
        name = "uk_cardapio_unidade_produto", columnNames = {"unidade_id", "produto_id"}))
public class ProdutoUnidadeEntidade {
    
    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(name = "produto_id", nullable = false)
    private UUID produtoId;

    @Column(nullable = false)
    private boolean ativo;

    protected ProdutoUnidadeEntidade() {}

    public ProdutoUnidadeEntidade(UUID id, UUID unidadeId, UUID produtoId, boolean ativo) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.produtoId = produtoId;
        this.ativo = ativo;
    }

    public UUID getId() { return id; }
    public UUID getUnidadeId() { return unidadeId; }
    public UUID getProdutoId() { return produtoId; }
    public boolean isAtivo() { return ativo; }
    public void alterarAtivo(boolean ativo) { this.ativo = ativo; }
}
