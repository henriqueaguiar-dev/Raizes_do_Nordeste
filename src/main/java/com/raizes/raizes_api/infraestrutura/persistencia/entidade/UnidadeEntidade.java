package com.raizes.raizes_api.infraestrutura.persistencia.entidade;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "unidades")
public class UnidadeEntidade {

    @Id 
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column (nullable = false, length = 255)
    private String endereco;

    @Column(nullable = false)
    private Boolean ativa;

    protected UnidadeEntidade() {
    }

    protected UnidadeEntidade(UUID id, String nome, String endereco, Boolean ativa) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.ativa = ativa;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    
}
