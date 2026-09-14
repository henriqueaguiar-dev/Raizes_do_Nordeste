package com.raizes.raizes_api.dominio.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.StatusPedido;

public class Pedido {

    private UUID id;
    private UUID ClienteId;
    private UUID unidadeId;
    private CanalPedido canalPedido;
    private StatusPedido status;
    private BigDecimal total;
    private List<PedidoItem> itens = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(UUID id, UUID clienteId, UUID unidadeId, CanalPedido canalPedido, List<PedidoItem> itens) {
        this.id = id;
        this.ClienteId = clienteId;
        this.unidadeId = unidadeId;
        this.canalPedido = canalPedido;
        this.status = StatusPedido.AGUARDANDO_PAGAMENTO;
        this.itens = itens;
        this.total = calcularTotal(itens);
    }

    private BigDecimal calcularTotal(List<PedidoItem> itens) {
        return itens.stream()
                .map(PedidoItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void marcarComoPago() {
        this.status = StatusPedido.PAGO;
    }

    public void cancelar() {
        this.status = StatusPedido.CANCELADO;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return ClienteId;
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

    public BigDecimal getTotal() {
        return total;
    }

    public List<PedidoItem> getItens() {
        return itens;
    }

}
