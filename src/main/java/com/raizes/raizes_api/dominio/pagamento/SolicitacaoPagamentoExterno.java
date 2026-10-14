package com.raizes.raizes_api.dominio.pagamento;

import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import java.math.BigDecimal;
import java.util.UUID;

public record SolicitacaoPagamentoExterno(UUID pedidoId, CanalPedido canalPedido,
        MetodoPagamento metodo, BigDecimal valor, String moeda, boolean aprovadoMock, int numeroTentativa) {
    public SolicitacaoPagamentoExterno(UUID pedidoId, CanalPedido canalPedido, MetodoPagamento metodo,
            BigDecimal valor, String moeda, boolean aprovadoMock) {
        this(pedidoId, canalPedido, metodo, valor, moeda, aprovadoMock, 1);
    }
}
