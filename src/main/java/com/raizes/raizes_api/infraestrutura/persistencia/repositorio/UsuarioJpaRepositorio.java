package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;

public interface UsuarioJpaRepositorio extends JpaRepository<UsuarioEntidade, UUID> {

    Optional<UsuarioEntidade> findByEmail(String email);

    boolean existsByEmail(String email);
}