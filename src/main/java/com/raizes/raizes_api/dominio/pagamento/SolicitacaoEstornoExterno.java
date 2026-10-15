package com.raizes.raizes_api.dominio.pagamento;
import java.math.BigDecimal;
import java.util.UUID;
public record SolicitacaoEstornoExterno(UUID pagamentoId, UUID pedidoId, String codigoTransacao,
        BigDecimal valor, String moeda) {}
