package com.raizes.raizes_api.dominio.modelo;

import java.util.UUID;

import com.raizes.raizes_api.dominio.enums.PerfilUsuario;

public class Usuario {
    private UUID id;
    private String nome;
    private String email;
    private String senhaHash;
    private PerfilUsuario perfil;
    private boolean ativo;
    private boolean consentimentoLgpd;

    public Usuario() {
    }

    public Usuario(UUID id, String nome, String email, String senhaHash, PerfilUsuario perfil, boolean ativo, boolean consentimentoLgpd) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
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

    public String getSenha() {
        return senhaHash;
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
