package com.raizes.raizes_api.dominio.pagamento;

public record ResultadoProvedorPagamento(int statusHttp, RetornoPagamentoExterno payload) {}
