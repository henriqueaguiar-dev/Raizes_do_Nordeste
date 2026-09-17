package com.raizes.raizes_api.infraestrutura.persistencia.mapeador;

import com.raizes.raizes_api.dominio.modelo.PedidoItem;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoItemEntidade;

public class PedidoitemMapeador {

    private PedidoitemMapeador(){
    }

    public static PedidoItem paraDominio(PedidoItemEntidade entidade){
        return new PedidoItem(
            entidade.getId(),
            entidade.getProdutoId(),
            entidade.getQuantidade(),
            entidade.getPrecoUnitario(),
            entidade.getSubtotal()
        );
    }

    public static PedidoItemEntidade paraEntidade(PedidoItem item){
        return new PedidoItemEntidade(
            item.getId(),
            item.getPedido
            item.getProdutoId(),
            item.getQuantidade(),
            item.getPrecoUnitario(),
            item.getSubTotal()
        );
    }
}
