package com.raizes.raizes_api.api.dto.resposta;

import com.raizes.raizes_api.dominio.enums.PerfilUsuario;

import java.util.UUID;

public class LoginResposta {

    private UUID usuarioId;
    private String nome;
    private String email;
    private PerfilUsuario perfil;
    private String accessToken;
    private String tokenType;
    private Long expiresIn;

    public LoginResposta(UUID usuarioId, String nome, String email, PerfilUsuario perfil,
                         String accessToken, String tokenType, Long expiresIn) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public UUID getUsuarioId() {
        return usuarioId;
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

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }
}