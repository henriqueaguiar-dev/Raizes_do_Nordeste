package com.raizes.raizes_api.infraestrutura.persistencia.entidade;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "auditorias")
public class AuditoriaEntidade {

    @Id 
    private UUID id;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(nullable = false, length = 120)
    private String acao;

    @Column(nullable = false, length = 120)
    private String recurso;

    @Column(name = "recurso_id")
    private UUID recursoId;

    @Column(nullable = false, length = 1000)
    private String detalhes;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    protected AuditoriaEntidade() {
    }

    public AuditoriaEntidade(UUID id, UUID usuarioId, String acao, String recurso, UUID recursoId, String detalhes, OffsetDateTime criadoEm) {
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
