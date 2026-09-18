package com.raizes.raizes_api.api.dto.resposta;

public class DetalheErroResposta {

    private String campo;
    private String problema;

    public DetalheErroResposta(String campo, String problema) {
        this.campo = campo;
        this.problema = problema;
    }

    public String getCampo() {
        return campo;
    }

    public String getProblema() {
        return problema;
    }
}
