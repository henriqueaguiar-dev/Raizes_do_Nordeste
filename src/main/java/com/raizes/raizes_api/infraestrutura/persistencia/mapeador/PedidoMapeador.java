package com.raizes.raizes_api.infraestrutura.persistencia.mapeador;

import com.raizes.raizes_api.dominio.modelo.Pedido;
import com.raizes.raizes_api.dominio.modelo.PedidoItem;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoItemEntidade;

import java.time.OffsetDateTime;
import java.util.List;

public class PedidoMapeador {

    private PedidoMapeador() {
    }

    public static Pedido paraDominio(PedidoEntidade entidade) {
        List<PedidoItem> itens = entidade.getItens()
                .stream()
                .map(PedidoItemMapeador::paraDominio)
                .toList();

        return new Pedido(
                entidade.getId(),
                entidade.getClienteId(),
                entidade.getUnidadeId(),
                entidade.getCanalPedido(),
                itens
        );
    }

    public static PedidoEntidade paraEntidade(Pedido pedido) {
        PedidoEntidade entidade = new PedidoEntidade(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getUnidadeId(),
                pedido.getCanalPedido(),
                pedido.getStatus(),
                pedido.getTotal(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        List<PedidoItemEntidade> itens = pedido.getItens()
                .stream()
                .map(PedidoItemMapeador::paraEntidade)
                .toList();

        itens.forEach(entidade::adicionarItem);

        return entidade;
    }
}