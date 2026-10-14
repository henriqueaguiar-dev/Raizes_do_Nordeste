package com.raizes.raizes_api.api.dto.resposta;

import java.util.UUID;
import java.math.BigDecimal;

public record FidelidadeSaldoResposta(UUID clienteId, long pontosGanhos, long pontosConvertidos,
        BigDecimal creditoConvertido, long restante, BigDecimal saldoCredito) {
    public FidelidadeSaldoResposta(UUID clienteId, long pontos) {
        this(clienteId, pontos, 0, BigDecimal.ZERO.setScale(2), pontos, BigDecimal.ZERO.setScale(2));
    }

    // Compatibilidade para consumidores Java do saldo anterior.
    public long pontos() { return restante; }
}
