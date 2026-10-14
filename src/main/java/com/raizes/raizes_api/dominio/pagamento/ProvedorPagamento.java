package com.raizes.raizes_api.dominio.pagamento;

public interface ProvedorPagamento {
    ResultadoProvedorPagamento solicitar(SolicitacaoPagamentoExterno solicitacao);
}
