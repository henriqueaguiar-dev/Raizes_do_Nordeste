package com.raizes.raizes_api.api.dto.resposta;

import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.StatusPedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PedidoResposta {

    private UUID id;
    private UUID clienteId;
    private UUID unidadeId;
    private CanalPedido canalPedido;
    private StatusPedido status;
    private BigDecimal subtotal;
    private BigDecimal valorDesconto;
    private BigDecimal total;
    private List<PedidoItemResposta> itens;

    public PedidoResposta(UUID id, UUID clienteId, UUID unidadeId, CanalPedido canalPedido,
                          StatusPedido status, BigDecimal subtotal, BigDecimal valorDesconto, BigDecimal total, List<PedidoItemResposta> itens) {
        this.id = id;
        this.clienteId = clienteId;
        this.unidadeId = unidadeId;
        this.canalPedido = canalPedido;
        this.status = status;
        this.subtotal = subtotal;
        this.valorDesconto = valorDesconto;
        this.total = total;
        this.itens = itens;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getUnidadeId() {
        return unidadeId;
    }

    public CanalPedido getCanalPedido() {
        return canalPedido;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getValorDesconto() { return valorDesconto; }

    public BigDecimal getTotal() {
        return total;
    }

    public List<PedidoItemResposta> getItens() {
        return itens;
    }
}