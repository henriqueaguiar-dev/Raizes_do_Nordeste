package com.raizes.raizes_api.infraestrutura.persistencia.entidade;

import java.util.UUID;

import com.raizes.raizes_api.dominio.enums.PerfilUsuario;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

public class UsuarioEntidade {

    @Id 
    private UUID id;
    
    @Column(nullable = false, length = 120)
    private String nome;

    @Column (nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerfilUsuario perfil;

    @Column (nullable = false)
    private boolean ativo;

    @Column (name = "consentimento_Lgpd", nullable = false)
    private boolean consentimentoLgpd;

    @Column (name = "criado_em", nullable = false)
    private long criadoEm;

    protected UsuarioEntidade() {
    }

    protected UsuarioEntidade(UUID id, String nome, String email, String senhaHash, PerfilUsuario perfil, boolean ativo, boolean consentimentoLgpd, long criadoEm) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
        this.ativo = ativo;
        this.consentimentoLgpd = consentimentoLgpd;
        this.criadoEm = criadoEm;
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

    public String getSenhaHash() {
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

    public long getCriadoEm() {
        return criadoEm;
    }
}
