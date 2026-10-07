package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UnidadeEntidade;

public interface UnidadeJpaRepositorio extends JpaRepository<UnidadeEntidade, UUID> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from UnidadeEntidade e where e.id = :id")
    java.util.Optional<UnidadeEntidade> buscarComBloqueio(@org.springframework.data.repository.query.Param("id") UUID id);
}
