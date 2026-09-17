package com.raizes.raizes_api.infraestrutura.persistencia.mapeador;

import java.time.OffsetDateTime;

import com.raizes.raizes_api.dominio.modelo.Usuario;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;

public class UsuarioMapeador {

    private UsuarioMapeador() {
    }

    public static Usuario paraDominio(UsuarioEntidade entidade){
        return new Usuario(
                entidade.getId(),
                entidade.getNome(),
                entidade.getEmail(),
                entidade.getSenhaHash(),
                entidade.getPerfil(),
                entidade.isAtivo(),
                entidade.isConsentimentoLgpd()
        );
    }

    public static UsuarioEntidade paraEntidade(Usuario usuario){
        return new UsuarioEntidade(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getSenha(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.isConsentimentoLgpd(),
                OffsetDateTime.now()
        );
    }
}
