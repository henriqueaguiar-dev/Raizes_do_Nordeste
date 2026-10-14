package com.raizes.raizes_api.dominio.excecao;

public class ProvedorPagamentoExcecao extends RuntimeException {
    public ProvedorPagamentoExcecao(String mensagem) { super(mensagem); }
    public ProvedorPagamentoExcecao(String mensagem, Throwable causa) { super(mensagem, causa); }
}
