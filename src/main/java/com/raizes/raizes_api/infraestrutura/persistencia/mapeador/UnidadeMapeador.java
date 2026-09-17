package com.raizes.raizes_api.infraestrutura.persistencia.mapeador;

import com.raizes.raizes_api.dominio.modelo.Unidade;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UnidadeEntidade;

public class UnidadeMapeador {

    private UnidadeMapeador() {
    }

    public static Unidade paraDominio(UnidadeEntidade entidade) {
        return new Unidade(
                entidade.getId(),
                entidade.getNome(),
                entidade.getEndereco(),
                entidade.isAtiva()
        );
    }

    public static UnidadeEntidade paraEntidade(Unidade unidade) {
        return new UnidadeEntidade(
                unidade.getId(),
                unidade.getNome(),
                unidade.getEndereco(),
                unidade.isAtiva()
        );
    }
}
