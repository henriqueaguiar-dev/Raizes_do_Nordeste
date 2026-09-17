package com.raizes.raizes_api.infraestrutura.persistencia.mapeador;

import com.raizes.raizes_api.dominio.modelo.Estoque;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.EstoqueEntidade;

public class EstoqueMapeador {

    private EstoqueMapeador(){
    }

    public static Estoque paraDominio(EstoqueEntidade entidade){
        return new Estoque(
            entidade.getId(),
            entidade.getUnidadeId(),
            entidade.getProdutoId(),
            entidade.getQuantidadeDisponivel()
        );
    }

    public static EstoqueEntidade paraEntidade(Estoque estoque){
        return new EstoqueEntidade(
            estoque.getId(),
            estoque.getUnidadeId(),
            estoque.getProdutoId(),
            estoque.getQuantidadeDisponivel()
        );
    }
}
