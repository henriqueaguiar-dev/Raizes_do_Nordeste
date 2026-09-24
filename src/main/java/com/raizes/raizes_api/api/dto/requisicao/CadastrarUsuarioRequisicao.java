package com.raizes.raizes_api.api.dto.requisicao;

import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CadastrarUsuarioRequisicao {

    @NotBlank(message = "O nome e obrigatorio.")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "O email e obrigatorio.")
    @Email(message = "O email deve ser valido.")
    @Size(max = 160, message = "O email deve ter no maximo 160 caracteres.")
    private String email;

    @NotBlank(message = "A senha e obrigatoria.")
    @Size(min = 8, max = 80, message = "A senha deve ter entre 8 e 80 caracteres.")
    private String senha;

    @NotNull(message = "O perfil e obrigatorio.")
    private PerfilUsuario perfil;

    @NotNull(message = "O consentimento LGPD e obrigatorio.")
    private Boolean consentimentoLgpd;

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public Boolean getConsentimentoLgpd() {
        return consentimentoLgpd;
    }
}