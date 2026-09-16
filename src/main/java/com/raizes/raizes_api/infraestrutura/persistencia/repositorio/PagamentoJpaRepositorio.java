package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PagamentoEntidade;

public interface PagamentoJpaRepositorio extends JpaRepository<PagamentoEntidade, UUID> {

    Optional<PagamentoEntidade> findByPedidoId(UUID pedidoId);

}
