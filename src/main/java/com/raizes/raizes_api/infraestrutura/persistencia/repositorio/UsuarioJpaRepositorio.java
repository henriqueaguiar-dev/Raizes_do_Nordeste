package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;

public interface UsuarioJpaRepositorio extends JpaRepository<UsuarioEntidade, UUID> {

    Optional<UsuarioEntidade> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UsuarioEntidade> findByPerfil(PerfilUsuario perfil);

    List<UsuarioEntidade> findByPerfilIn(List<PerfilUsuario> perfis);
}