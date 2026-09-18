package com.raizes.raizes_api.infraestrutura.persistencia.mapeador;

import com.raizes.raizes_api.dominio.modelo.Auditoria;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.AuditoriaEntidade;

public class AuditoriaMapeador {

    private AuditoriaMapeador() {
    }

    public static Auditoria paraDominio(AuditoriaEntidade entidade) {
        return new Auditoria(
                entidade.getId(),
                entidade.getUsuarioId(),
                entidade.getAcao(),
                entidade.getRecurso(),
                entidade.getRecursoId(),
                entidade.getDetalhes(),
                entidade.getCriadoEm()
        );
    }

    public static AuditoriaEntidade paraEntidade(Auditoria auditoria) {
        return new AuditoriaEntidade(
                auditoria.getId(),
                auditoria.getUsuarioId(),
                auditoria.getAcao(),
                auditoria.getRecurso(),
                auditoria.getRecursoId(),
                auditoria.getDetalhes(),
                auditoria.getCriadoEm()
        );
    }
}
