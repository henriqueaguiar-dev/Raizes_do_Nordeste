package com.raizes.raizes_api.dominio.excecao;

public class RecursoNaoEncontradoExcecao extends RuntimeException {

    public RecursoNaoEncontradoExcecao(String mensagem) {
        super(mensagem);
    }
}
