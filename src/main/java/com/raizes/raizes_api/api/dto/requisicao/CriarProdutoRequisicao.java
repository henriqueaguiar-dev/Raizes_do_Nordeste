package com.raizes.raizes_api.api.dto.requisicao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CriarProdutoRequisicao {

    @NotBlank(message = "O nome do produto e obrigatorio.")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "A descricao do produto e obrigatoria.")
    @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres.")
    private String descricao;

    @NotNull(message = "O preco do produto e obrigatorio.")
    @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero.")
    private BigDecimal preco;

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
