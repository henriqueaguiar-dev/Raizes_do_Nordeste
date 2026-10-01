package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarUsuarioRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioInternoRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.dominio.excecao.AcessoNegadoExcecao;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @PutMapping("/{id}")
    public UsuarioResposta atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarUsuarioRequisicao requisicao,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID usuarioAutenticadoId = UUID.fromString(jwt.getSubject());
        boolean administrador = "ADMIN".equals(jwt.getClaimAsString("perfil"));

        if (!administrador && !id.equals(usuarioAutenticadoId)) {
            throw new AcessoNegadoExcecao("Voce nao tem permissao para atualizar este usuario.");
        }

        return usuarioServico.atualizar(id, requisicao);
    }

    @PostMapping("/internos")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrarInterno(@Valid @RequestBody CadastrarUsuarioInternoRequisicao requisicao) {
        return usuarioServico.cadastrarInterno(requisicao);
    }
}