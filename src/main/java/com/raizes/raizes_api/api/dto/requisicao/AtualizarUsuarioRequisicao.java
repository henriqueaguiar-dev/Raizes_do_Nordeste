package com.raizes.raizes_api.api.dto.requisicao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AtualizarUsuarioRequisicao {

    @NotBlank(message = "O email e obrigatorio.")
    @Email(message = "O email deve ser valido.")
    @Size(max = 160)
    private String email;

    @NotBlank(message = "A senha e obrigatoria.")
    @Size(min = 8, max = 72)
    private String senha;

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}