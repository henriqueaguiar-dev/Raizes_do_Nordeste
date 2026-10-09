package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.LoginRequisicao;
import com.raizes.raizes_api.api.dto.resposta.LoginResposta;
import com.raizes.raizes_api.aplicacao.servico.AutenticacaoServico;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "01 - Autenticar",
    description = "Login usuário"
)

@RestController
@RequestMapping("/auth")
public class AutenticacaoControlador {

    private final AutenticacaoServico autenticacaoServico;

    public AutenticacaoControlador(AutenticacaoServico autenticacaoServico) {
        this.autenticacaoServico = autenticacaoServico;
    }

    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @PostMapping("/login")
    public LoginResposta login(@Valid @RequestBody LoginRequisicao requisicao) {
        return autenticacaoServico.login(requisicao);
    }
}
