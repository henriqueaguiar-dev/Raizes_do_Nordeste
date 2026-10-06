package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.resposta.FidelidadeSaldoResposta;
import com.raizes.raizes_api.aplicacao.servico.FidelidadeServico;
import com.raizes.raizes_api.dominio.excecao.AcessoNegadoExcecao;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/fidelidade")
public class FidelidadeControlador {
    private final FidelidadeServico fidelidadeServico;

    public FidelidadeControlador(FidelidadeServico fidelidadeServico) {
        this.fidelidadeServico = fidelidadeServico;
    }

    @GetMapping("/saldo")
    public FidelidadeSaldoResposta consultarSaldo(@AuthenticationPrincipal Jwt jwt) {
        return fidelidadeServico.consultarSaldo(UUID.fromString(jwt.getSubject()));
    }

    @GetMapping("/saldo/{clienteId}")
    public FidelidadeSaldoResposta consultarSaldoPorCliente(@PathVariable UUID clienteId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        boolean administrador = "ADMIN".equals(jwt.getClaimAsString("perfil"));
        if (!administrador && !clienteId.equals(usuarioId)) {
            throw new AcessoNegadoExcecao("Voce nao tem permissao para consultar os pontos deste usuario.");
        }
        return fidelidadeServico.consultarSaldo(clienteId);
    }
}
