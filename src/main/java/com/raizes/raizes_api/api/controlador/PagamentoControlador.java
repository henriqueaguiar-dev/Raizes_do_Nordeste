package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.ProcessarPagamentoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.PagamentoResposta;
import com.raizes.raizes_api.aplicacao.servico.PagamentoServico;
import com.raizes.raizes_api.dominio.excecao.CredenciaisInvalidasExcecao;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
    name = "07 - Pagamentos",
    description = "Gerenciamento de pagamentos"
)

@RestController
@RequestMapping("/pagamentos")
public class PagamentoControlador {

    private final PagamentoServico pagamentoServico;

    public PagamentoControlador(PagamentoServico pagamentoServico) {
        this.pagamentoServico = pagamentoServico;
    }

    @PostMapping("/pedidos/{pedidoId}")
    public PagamentoResposta processar(
            @PathVariable UUID pedidoId,
            @Valid @RequestBody ProcessarPagamentoRequisicao requisicao,
            @AuthenticationPrincipal Jwt jwt) {

        UUID usuarioId = obterUsuarioId(jwt);
        return pagamentoServico.processar(
                pedidoId,
                requisicao,
                usuarioId,
                jwt.getClaimAsString("perfil"));
    }

    @GetMapping("/pedidos/{pedidoId}")
    public PagamentoResposta buscarPorPedido(
            @PathVariable UUID pedidoId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID usuarioId = obterUsuarioId(jwt);
        return pagamentoServico.buscarPorPedido(
                pedidoId,
                usuarioId,
                jwt.getClaimAsString("perfil"));
    }

    private UUID obterUsuarioId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new CredenciaisInvalidasExcecao("Token sem identificacao valida de usuario.");
        }

        String subject = jwt.getSubject();
        try {
            UUID usuarioId = UUID.fromString(subject);
            // UUID.fromString aceita grupos abreviados; exija o formato completo.
            if (!usuarioId.toString().equalsIgnoreCase(subject)) {
                throw new IllegalArgumentException("UUID fora do formato esperado.");
            }
            return usuarioId;
        } catch (IllegalArgumentException excecao) {
            throw new CredenciaisInvalidasExcecao("Token sem identificacao valida de usuario.");
        }
    }
}
