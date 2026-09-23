package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.AuditoriaEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.AuditoriaJpaRepositorio;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuditoriaServico {

    private final AuditoriaJpaRepositorio auditoriaRepositorio;

    public AuditoriaServico(AuditoriaJpaRepositorio auditoriaRepositorio) {
        this.auditoriaRepositorio = auditoriaRepositorio;
    }

    public void registrar(UUID usuarioId, String acao, String recurso, UUID recursoId, String detalhes) {
        AuditoriaEntidade auditoria = new AuditoriaEntidade(
                UUID.randomUUID(),
                usuarioId,
                acao,
                recurso,
                recursoId,
                detalhes,
                OffsetDateTime.now()
        );

        auditoriaRepositorio.save(auditoria);
    }
}