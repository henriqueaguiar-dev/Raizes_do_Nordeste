package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.ProcessarPagamentoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.PagamentoResposta;
import com.raizes.raizes_api.aplicacao.servico.PagamentoServico;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
            @Valid @RequestBody ProcessarPagamentoRequisicao requisicao
    ) {
        return pagamentoServico.processar(pedidoId, requisicao);
    }

    @GetMapping("/pedidos/{pedidoId}")
    public PagamentoResposta buscarPorPedido(@PathVariable UUID pedidoId) {
        return pagamentoServico.buscarPorPedido(pedidoId);
    }
}