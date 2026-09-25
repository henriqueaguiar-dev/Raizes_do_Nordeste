package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarStatusPedidoRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CriarPedidoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.PedidoResposta;
import com.raizes.raizes_api.aplicacao.servico.PedidoServico;
import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.StatusPedido;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
public class PedidoControlador {

    private final PedidoServico pedidoServico;

    public PedidoControlador(PedidoServico pedidoServico) {
        this.pedidoServico = pedidoServico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResposta criar(
            @Valid @RequestBody CriarPedidoRequisicao requisicao,
            @AuthenticationPrincipal Jwt jwt) {
        UUID clienteId = UUID.fromString(jwt.getSubject());
        return pedidoServico.criar(clienteId, requisicao);
    }

    @GetMapping
    public List<PedidoResposta> listar(
            @RequestParam(required = false) CanalPedido canalPedido,
            @RequestParam(required = false) StatusPedido status,
            @AuthenticationPrincipal Jwt jwt) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        String perfil = jwt.getClaimAsString("perfil");

        return pedidoServico.listar(usuarioId, perfil, canalPedido, status);
    }


    @GetMapping("/{id}")
    public PedidoResposta buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        String perfil = jwt.getClaimAsString("perfil");

        return pedidoServico.buscarPorId(id, usuarioId, perfil);
    }

    @PatchMapping("/{id}/status")
    public PedidoResposta atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusPedidoRequisicao requisicao) {
        return pedidoServico.atualizarStatus(id, requisicao.getStatus());
    }
}
