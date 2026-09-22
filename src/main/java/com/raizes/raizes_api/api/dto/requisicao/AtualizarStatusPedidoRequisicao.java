package com.raizes.raizes_api.api.dto.requisicao;

import com.raizes.raizes_api.dominio.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public class AtualizarStatusPedidoRequisicao {

    @NotNull(message = "O status do pedido e obrigatorio.")
    private StatusPedido status;

    public StatusPedido getStatus() {
        return status;
    }
}
