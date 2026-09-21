package com.raizes.raizes_api.api.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AtualizarUnidadeRequisicao {

    @NotBlank(message = "O nome da unidade e obrigatorio.")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "O endereco da unidade e obrigatorio.")
    @Size(max = 255, message = "O endereco deve ter no maximo 255 caracteres.")
    private String endereco;

    @NotNull(message = "O status ativa e obrigatorio.")
    private Boolean ativa;

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public Boolean getAtiva() {
        return ativa;
    }
}
