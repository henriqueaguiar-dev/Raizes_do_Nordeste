package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import com.raizes.raizes_api.infraestrutura.persistencia.entidade.FidelidadeResgateEntidade;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface FidelidadeResgateJpaRepositorio extends JpaRepository<FidelidadeResgateEntidade, UUID> {
    @Query("select sum(r.valorCredito) from FidelidadeResgateEntidade r where r.clienteId = :clienteId")
    java.math.BigDecimal somarCredito(@Param("clienteId") UUID clienteId);
    @Query("select coalesce(sum(r.pontos), 0) from FidelidadeResgateEntidade r where r.clienteId = :clienteId")
    long somarPontos(@Param("clienteId") UUID clienteId);
    List<FidelidadeResgateEntidade> findByClienteIdOrderByCriadoEmDesc(UUID clienteId);
}
