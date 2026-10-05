package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarUsuarioRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioInternoRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.aplicacao.servico.UsuarioInternoServico;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.dominio.excecao.AcessoNegadoExcecao;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControlador {

    private final UsuarioServico usuarioServico;
    private final UsuarioInternoServico usuarioInternoServico;

    public UsuarioControlador(UsuarioServico usuarioServico, UsuarioInternoServico usuarioInternoServico) {
        this.usuarioServico = usuarioServico;
        this.usuarioInternoServico = usuarioInternoServico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrar(@Valid @RequestBody CadastrarUsuarioRequisicao requisicao) {
        return usuarioServico.cadastrar(requisicao);
    }

    @GetMapping
    public List<UsuarioResposta> listarClientes() {
        return usuarioServico.listarCliente();
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        UUID usuarioAutenticadoId = UUID.fromString(jwt.getSubject());
        boolean administrador = "ADMIN".equals(jwt.getClaimAsString("perfil"));

        if (!administrador && !id.equals(usuarioAutenticadoId)) {
            throw new AcessoNegadoExcecao("Voce nao tem permissao para desativar este usuario.");
        }

        usuarioServico.desativar(id);
    }

    @PostMapping("/internos")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrarInterno(@Valid @RequestBody CadastrarUsuarioInternoRequisicao requisicao) {
        return usuarioInternoServico.cadastrarInterno(requisicao);
    }

    @GetMapping("/internos")
    public List<UsuarioResposta> listarColaboradores() {
        return usuarioServico.listarColaboradores();
    }
}