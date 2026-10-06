package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PagamentoEntidade;

public interface PagamentoJpaRepositorio extends JpaRepository<PagamentoEntidade, UUID> {

    Optional<PagamentoEntidade> findByPedidoId(UUID pedidoId);

    @Query("""
            select sum(pagamento.valor)
            from PagamentoEntidade pagamento, PedidoEntidade pedido
            where pagamento.pedidoId = pedido.id
              and pedido.clienteId = :clienteId
              and pagamento.status = :status
            """)
    BigDecimal somarValorPorClienteEStatus(@Param("clienteId") UUID clienteId,
                                          @Param("status") StatusPagamento status);

}
