package com.raizes.raizes_api.api.dto.resposta;

import com.raizes.raizes_api.dominio.enums.PerfilUsuario;

import java.util.UUID;

public class LoginResposta {

    private UUID usuarioId;
    private String nome;
    private String email;
    private PerfilUsuario perfil;
    private String mensagem;

    public LoginResposta(UUID usuarioId, String nome, String email, PerfilUsuario perfil, String mensagem) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.mensagem = mensagem;
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

    public String getMensagem() {
        return mensagem;
    }
}
