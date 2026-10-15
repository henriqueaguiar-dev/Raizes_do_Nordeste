package com.raizes.raizes_api.dominio.pagamento;
import java.math.BigDecimal;
import java.util.UUID;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;
public record RetornoEstornoExterno(UUID pagamentoId, String codigoEstorno, StatusPagamento status,
        BigDecimal valor, String moeda, String mensagem) {}
