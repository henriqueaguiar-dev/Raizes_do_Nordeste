package com.raizes.raizes_api.dominio.excecao;

public class CredenciaisInvalidasExcecao extends RuntimeException {

    public CredenciaisInvalidasExcecao(String mensagem) {
        super(mensagem);
    }
}
