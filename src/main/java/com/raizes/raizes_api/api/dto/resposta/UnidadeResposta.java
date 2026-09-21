package com.raizes.raizes_api.api.dto.resposta;

import java.util.UUID;

public class UnidadeResposta {

    private UUID id;
    private String nome;
    private String endereco;
    private boolean ativa;

    public UnidadeResposta(UUID id, String nome, String endereco, boolean ativa) {
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

    public boolean isAtiva() {
        return ativa;
    }
}
