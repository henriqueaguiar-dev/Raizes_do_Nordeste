package com.raizes.raizes_api.api.dto.requisicao;

import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import jakarta.validation.constraints.NotNull;

public class ProcessarPagamentoRequisicao {

    @NotNull(message = "O metodo de pagamento e obrigatorio.")
    private MetodoPagamento metodo;

    @NotNull(message = "O campo aprovadoMock e obrigatorio.")
    private Boolean aprovadoMock;

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public Boolean getAprovadoMock() {
        return aprovadoMock;
    }
}
