package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.LoginRequisicao;
import com.raizes.raizes_api.api.dto.resposta.LoginResposta;
import com.raizes.raizes_api.aplicacao.servico.AutenticacaoServico;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AutenticacaoControlador {

    private final AutenticacaoServico autenticacaoServico;

    public AutenticacaoControlador(AutenticacaoServico autenticacaoServico) {
        this.autenticacaoServico = autenticacaoServico;
    }

    @PostMapping("/login")
    public LoginResposta login(@Valid @RequestBody LoginRequisicao requisicao) {
        return autenticacaoServico.login(requisicao);
    }
}
