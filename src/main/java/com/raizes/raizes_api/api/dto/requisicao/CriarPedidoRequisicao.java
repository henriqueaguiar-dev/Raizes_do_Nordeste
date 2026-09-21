package com.raizes.raizes_api.api.dto.requisicao;

import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class CriarPedidoRequisicao {

    @NotNull(message = "O cliente e obrigatorio.")
    private UUID clienteId;

    @NotNull(message = "A unidade e obrigatoria.")
    private UUID unidadeId;

    @NotNull(message = "O canal do pedido e obrigatorio.")
    private CanalPedido canalPedido;

    @NotNull(message = "A forma de pagamento e obrigatoria.")
    private MetodoPagamento formaPagamento;

    @Valid
    @NotEmpty(message = "O pedido deve ter pelo menos um item.")
    private List<CriarPedidoItemRequisicao> itens;

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getUnidadeId() {
        return unidadeId;
    }

    public CanalPedido getCanalPedido() {
        return canalPedido;
    }

    public MetodoPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public List<CriarPedidoItemRequisicao> getItens() {
        return itens;
    }
}
