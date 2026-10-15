package com.raizes.raizes_api.dominio.pagamento;

public interface ProvedorPagamento {
    ResultadoEstorno estornar(SolicitacaoEstornoExterno solicitacao);
    ResultadoProvedorPagamento solicitar(SolicitacaoPagamentoExterno solicitacao);
}
