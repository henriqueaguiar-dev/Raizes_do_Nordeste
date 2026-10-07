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
    private BigDecimal subtotal;
    private BigDecimal valorDesconto;
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
        this.subtotal = calcularTotal(itens).setScale(2, java.math.RoundingMode.HALF_UP);
        this.valorDesconto = canalPedido == CanalPedido.APP || canalPedido == CanalPedido.WEB
                ? subtotal.multiply(new BigDecimal("0.10")).setScale(2, java.math.RoundingMode.HALF_UP)
                : new BigDecimal("0.00");
        this.total = subtotal.subtract(valorDesconto);
    }

    public Pedido(UUID id, UUID clienteId, UUID unidadeId, CanalPedido canalPedido, List<PedidoItem> itens,
            StatusPedido status, BigDecimal subtotal, BigDecimal valorDesconto, BigDecimal total) {
        this(id, clienteId, unidadeId, canalPedido, itens);
        this.status = status;
        this.subtotal = subtotal;
        this.valorDesconto = valorDesconto;
        this.total = total;
    }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getValorDesconto() { return valorDesconto; }

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
