package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.StatusPedido;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoEntidade;

public interface PedidoJpaRepositorio extends JpaRepository<PedidoEntidade, UUID> {

    List<PedidoEntidade> findByClienteId(UUID clienteId);

    List<PedidoEntidade> findByCanalPedido(CanalPedido canalPedido);

    List<PedidoEntidade> findByStatus(StatusPedido statusPedido);

    List<PedidoEntidade> findByCanalPedidoAndStatus(CanalPedido canalPedido, StatusPedido status);

}
