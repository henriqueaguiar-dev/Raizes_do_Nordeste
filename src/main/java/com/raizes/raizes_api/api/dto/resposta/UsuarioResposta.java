package com.raizes.raizes_api.api.dto.resposta;

import com.raizes.raizes_api.dominio.enums.PerfilUsuario;

import java.util.UUID;

public class UsuarioResposta {

    private UUID id;
    private String nome;
    private String email;
    private PerfilUsuario perfil;
    private boolean ativo;
    private boolean consentimentoLgpd;

    public UsuarioResposta(UUID id, String nome, String email, PerfilUsuario perfil,
                           boolean ativo, boolean consentimentoLgpd) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.ativo = ativo;
        this.consentimentoLgpd = consentimentoLgpd;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean isConsentimentoLgpd() {
        return consentimentoLgpd;
    }
}
