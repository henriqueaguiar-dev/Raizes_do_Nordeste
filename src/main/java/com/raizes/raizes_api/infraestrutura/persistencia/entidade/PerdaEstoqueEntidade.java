package com.raizes.raizes_api.infraestrutura.persistencia.entidade;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity
@Table(name="perdas_estoque")
public class PerdaEstoqueEntidade {

    @Id 
    private UUID id;

    @Column(nullable=false) 
    private UUID pedidoId;

    @Column(nullable=false) 
    private UUID unidadeId;

    @Column(nullable=false) 
    private UUID produtoId;

    @Column(nullable=false) 
    private int quantidade;

    @Column(nullable=false) 
    private String motivo;

    @Column(nullable=false) 
    private OffsetDateTime criadoEm;

    protected PerdaEstoqueEntidade() {}
    public PerdaEstoqueEntidade(UUID pedidoId, UUID unidadeId, UUID produtoId, int quantidade) {
        this.id=UUID.randomUUID();this.pedidoId=pedidoId;this.unidadeId=unidadeId;
        this.produtoId=produtoId;this.quantidade=quantidade;
        this.motivo="CANCELAMENTO_APOS_INICIO_PREPARO";this.criadoEm=OffsetDateTime.now();
    }

    public int getQuantidade() { return quantidade; }

    public UUID getProdutoId() { return produtoId; }
}
