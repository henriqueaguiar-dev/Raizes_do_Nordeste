package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.EstoqueEntidade;

public interface EstoqueJpaRepositorio extends JpaRepository<EstoqueEntidade, UUID> {

}
