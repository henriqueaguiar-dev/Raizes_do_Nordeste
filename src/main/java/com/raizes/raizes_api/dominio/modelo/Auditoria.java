package com.raizes.raizes_api.dominio.modelo;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Auditoria {

    private UUID id;
    private UUID usuarioId;
    private String acao;
    private String recurso;
    private UUID recursoId;
    private String detalhes;
    private OffsetDateTime criadoEm;

    public Auditoria() {
    }

    public Auditoria(UUID id, UUID usuarioId, String acao, String recurso,
                     UUID recursoId, String detalhes, OffsetDateTime criadoEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.recurso = recurso;
        this.recursoId = recursoId;
        this.detalhes = detalhes;
        this.criadoEm = criadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getAcao() {
        return acao;
    }

    public String getRecurso() {
        return recurso;
    }

    public UUID getRecursoId() {
        return recursoId;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }
}