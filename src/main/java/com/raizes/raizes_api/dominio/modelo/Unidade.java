package com.raizes.raizes_api.dominio.modelo;

import java.util.UUID;

public class Unidade {
    
    private UUID id;
    private String codigoUnidade;
    private String nome;
    private String endereco;
    private boolean ativa;

    public Unidade() {
    }

    public Unidade(UUID id, String codigoUnidade, String nome, String endereco, boolean ativa) {
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

    public boolean isAtiva() {
        return ativa;
    }
    

}
