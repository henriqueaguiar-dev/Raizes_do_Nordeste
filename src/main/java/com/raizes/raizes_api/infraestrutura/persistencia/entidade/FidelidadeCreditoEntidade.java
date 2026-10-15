package com.raizes.raizes_api.infraestrutura.persistencia.entidade;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fidelidade_creditos", uniqueConstraints = @UniqueConstraint(name = "uk_fidelidade_pagamento", columnNames = "pagamento_id"))
public class FidelidadeCreditoEntidade {
    @Id private UUID id;
    @Column(name = "cliente_id", nullable = false) private UUID clienteId;
    @Column(name = "pagamento_id", nullable = false) private UUID pagamentoId;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal valor;
    @Column(name = "criado_em", nullable = false) private OffsetDateTime criadoEm;
    @Column(nullable=false) private boolean estornado = false;
    public void estornar() { this.estornado = true; }
    public boolean isEstornado() { return estornado; }
    protected FidelidadeCreditoEntidade() {}
    public FidelidadeCreditoEntidade(UUID clienteId, UUID pagamentoId, BigDecimal valor) {
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.pagamentoId = pagamentoId;
        this.valor = valor;
        this.criadoEm = OffsetDateTime.now();
    }
}
