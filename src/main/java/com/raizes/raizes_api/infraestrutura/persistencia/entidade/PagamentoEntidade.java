package com.raizes.raizes_api.infraestrutura.persistencia.entidade;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity 
@Table(name = "pagamentos", uniqueConstraints = {
    @UniqueConstraint(name = "uk_pagamento_pedido", columnNames = "pedido_id")
})
public class PagamentoEntidade {

    @Id 
    private UUID id;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPagamento status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetodoPagamento metodo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "codigo_trasacao_mock", length = 120)
    private String codigoTransacaoMock;
    
    @Column(name = "resposta_mock", length = 1000)
    private String respostaMock;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "valor_fidelidade", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFidelidade = BigDecimal.ZERO;

    public void definirValorFidelidade(BigDecimal valor) { this.valorFidelidade = valor; }
    public BigDecimal getValorFidelidade() { return valorFidelidade; }
    public BigDecimal getValorExterno() { return valor.subtract(valorFidelidade); }

    @jakarta.persistence.Lob
    @Column(name = "solicitacao_servico")
    private String solicitacaoServico;
    @jakarta.persistence.Lob
    @Column(name = "retorno_servico")
    private String retornoServico;
    @Column(name = "status_http_servico")
    private Integer statusHttpServico;

    public void registrarIntegracao(String solicitacao, String retorno, int statusHttp) {
        this.solicitacaoServico = solicitacao;
        this.retornoServico = retorno;
        this.statusHttpServico = statusHttp;
    }
    public String getSolicitacaoServico() { return solicitacaoServico; }
    public String getRetornoServico() { return retornoServico; }
    public Integer getStatusHttpServico() { return statusHttpServico; }

    protected PagamentoEntidade() {
    }

    public PagamentoEntidade(UUID id, UUID pedidoId, StatusPagamento status, MetodoPagamento metodo, BigDecimal valor, String codigoTransacaoMock, String respostaMock, OffsetDateTime criadoEm) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.status = status;
        this.metodo = metodo;
        this.valor = valor;
        this.codigoTransacaoMock = codigoTransacaoMock;
        this.respostaMock = respostaMock;
        this.criadoEm = criadoEm;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getCodigoTransacaoMock() {
        return codigoTransacaoMock;
    }

    public String getRespostaMock() {
        return respostaMock;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }
}
