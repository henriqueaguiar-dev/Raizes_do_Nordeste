package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.EstoqueEntidade;

public interface EstoqueJpaRepositorio extends JpaRepository<EstoqueEntidade, UUID> {

    Optional<EstoqueEntidade> findByUnidadeIdAndProdutoId(UUID unidadeId, UUID produtoId);

    List<EstoqueEntidade> findByUnidadeId(UUID unidadeId);;
}
