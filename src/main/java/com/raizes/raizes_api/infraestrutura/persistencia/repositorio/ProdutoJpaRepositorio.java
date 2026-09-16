package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizes.raizes_api.dominio.modelo.Produto;

public interface ProdutoJpaRepositorio extends JpaRepository<Produto, UUID> {

}
