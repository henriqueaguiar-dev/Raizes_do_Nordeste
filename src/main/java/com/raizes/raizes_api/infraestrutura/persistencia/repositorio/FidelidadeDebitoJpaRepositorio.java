package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.FidelidadeDebitoEntidade;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.UUID;

public interface FidelidadeDebitoJpaRepositorio extends JpaRepository<FidelidadeDebitoEntidade, UUID> {
    java.util.Optional<FidelidadeDebitoEntidade> findByPagamentoId(UUID pagamentoId);
    @Query("select sum(d.valor) from FidelidadeDebitoEntidade d where d.clienteId = :clienteId and d.estornado = false")
    BigDecimal somarValor(@Param("clienteId") UUID clienteId);
}
