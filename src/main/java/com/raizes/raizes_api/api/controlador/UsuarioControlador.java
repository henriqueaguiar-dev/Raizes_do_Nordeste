package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControlador {

    private final UsuarioServico usuarioServico;

    public UsuarioControlador(UsuarioServico usuarioServico) {
        this.usuarioServico = usuarioServico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrar(@Valid @RequestBody CadastrarUsuarioRequisicao requisicao) {
        return usuarioServico.cadastrar(requisicao);
    }
}