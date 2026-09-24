package com.raizes.raizes_api.api.dto.requisicao;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequisicao {

    @NotBlank(message = "O email e obrigatorio.")
    @Email(message = "O email deve ser valido.")
    private String email;

    @NotBlank(message = "A senha e obrigatoria.")
    private String senha;

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }
}
