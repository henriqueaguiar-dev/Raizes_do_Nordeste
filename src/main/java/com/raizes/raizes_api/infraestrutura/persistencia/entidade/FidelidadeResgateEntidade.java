package com.raizes.raizes_api.infraestrutura.persistencia.entidade;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "fidelidade_resgates")
public class FidelidadeResgateEntidade {
    @Id 
    private UUID id;

    @Column(name = "cliente_id", nullable = false) 
    private UUID clienteId;

    @Column(nullable = false) 
    private long pontos;

    @Column(nullable = false, length = 80) 
    private String beneficio;

    @Column(name = "criado_em", nullable = false) 
    private OffsetDateTime criadoEm;

    @Column(name = "valor_credito", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorCredito = BigDecimal.ZERO;

    protected FidelidadeResgateEntidade() {}
    public FidelidadeResgateEntidade(UUID clienteId, long pontos, String beneficio, BigDecimal valorCredito) {
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.pontos = pontos;
        this.beneficio = beneficio;
        this.valorCredito = valorCredito;
        this.criadoEm = OffsetDateTime.now();
    }
    
    public BigDecimal getValorCredito() { return valorCredito; }
    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public long getPontos() { return pontos; }
    public String getBeneficio() { return beneficio; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}
