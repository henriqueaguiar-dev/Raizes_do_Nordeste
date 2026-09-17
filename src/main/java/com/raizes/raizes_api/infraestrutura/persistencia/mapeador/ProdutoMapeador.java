package com.raizes.raizes_api.infraestrutura.persistencia.mapeador;

import com.raizes.raizes_api.dominio.modelo.Produto;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.ProdutoEntidade;

public class ProdutoMapeador {

    private ProdutoMapeador(){
    }

    public static Produto paraDominio(ProdutoEntidade entidade){
        return new Produto(
            entidade.getId(),
            entidade.getNome(),
            entidade.getDescricao(),
            entidade.getPreco(),
            entidade.isAtivo()
        );
    }

    public static ProdutoEntidade paraEntidade(Produto produto){
        return new ProdutoEntidade(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            produto.isAtivo()
        );
    }
}
