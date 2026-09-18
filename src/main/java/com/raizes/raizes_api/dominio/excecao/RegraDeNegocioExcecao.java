package com.raizes.raizes_api.dominio.excecao;

public class RegraDeNegocioExcecao extends RuntimeException {

    public RegraDeNegocioExcecao(String mensagem) {
        super(mensagem);
    }
}