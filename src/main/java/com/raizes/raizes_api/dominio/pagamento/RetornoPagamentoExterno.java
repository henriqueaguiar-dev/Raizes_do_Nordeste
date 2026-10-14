package com.raizes.raizes_api.dominio.pagamento;

import com.raizes.raizes_api.dominio.enums.StatusPagamento;
import java.math.BigDecimal;
import java.util.UUID;

public record RetornoPagamentoExterno(UUID pedidoId, String codigoTransacao,
        StatusPagamento status, BigDecimal valor, String moeda, String mensagem) {}
