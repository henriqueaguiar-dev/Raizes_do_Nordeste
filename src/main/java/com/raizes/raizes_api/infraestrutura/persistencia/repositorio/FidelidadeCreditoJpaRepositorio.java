package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.FidelidadeCreditoEntidade;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.UUID;

public interface FidelidadeCreditoJpaRepositorio extends JpaRepository<FidelidadeCreditoEntidade, UUID> {
    boolean existsByPagamentoId(UUID pagamentoId);
    java.util.Optional<FidelidadeCreditoEntidade> findByPagamentoId(UUID pagamentoId);
    @Query("select sum(c.valor) from FidelidadeCreditoEntidade c where c.clienteId = :clienteId and c.estornado = false")
    BigDecimal somarValor(@Param("clienteId") UUID clienteId);
}
