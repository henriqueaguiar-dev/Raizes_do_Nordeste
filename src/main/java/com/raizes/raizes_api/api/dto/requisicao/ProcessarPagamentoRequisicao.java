package com.raizes.raizes_api.api.dto.requisicao;

import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import jakarta.validation.constraints.NotNull;

public class ProcessarPagamentoRequisicao {

    @NotNull(message = "O metodo de pagamento e obrigatorio.")
    private MetodoPagamento metodo;

    @NotNull(message = "O campo aprovadoMock e obrigatorio.")
    private Boolean aprovadoMock;

    @jakarta.validation.constraints.DecimalMin(value = "0.00", message = "Credito de fidelidade nao pode ser negativo.")
    @jakarta.validation.constraints.Digits(integer = 8, fraction = 2, message = "Credito deve ter ate duas casas decimais.")
    private java.math.BigDecimal valorFidelidade;

    public java.math.BigDecimal getValorFidelidade() {
        return valorFidelidade == null ? java.math.BigDecimal.ZERO : valorFidelidade;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public Boolean getAprovadoMock() {
        return aprovadoMock;
    }
}
