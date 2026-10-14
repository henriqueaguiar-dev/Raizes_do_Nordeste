package com.raizes.raizes_api.api.dto.resposta;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.math.BigDecimal;

public record FidelidadeResgateResposta(UUID id, UUID clienteId, long pontosConsumidos,
        String beneficio, String codigoResgate, OffsetDateTime criadoEm, BigDecimal valorCredito) {}
