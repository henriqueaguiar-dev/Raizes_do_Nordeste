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

    @Column(name = "codigo_unidade", nullable = false, unique = true, length = 30)
    private String codigoUnidade;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 255)
    private String endereco;

    @Column(nullable = false)
    private Boolean ativa;

    protected UnidadeEntidade() {
    }

    public UnidadeEntidade(UUID id, String codigoUnidade, String nome, String endereco, Boolean ativa) {
        this.id = id;
        this.codigoUnidade = codigoUnidade;
        this.nome = nome;
        this.endereco = endereco;
        this.ativa = ativa;
    }

    public UUID getId() {
        return id;
    }

    public String getCodigoUnidade() {
        return codigoUnidade;
    }   

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public Boolean isAtiva() {
        return ativa;
    }

}
