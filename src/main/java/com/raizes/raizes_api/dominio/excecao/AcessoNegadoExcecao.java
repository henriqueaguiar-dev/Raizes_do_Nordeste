package com.raizes.raizes_api.dominio.excecao;

public class AcessoNegadoExcecao extends RuntimeException {

    public AcessoNegadoExcecao(String mensagem) {
        super(mensagem);
    }
}