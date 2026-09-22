package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.CriarPedidoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.PedidoResposta;
import com.raizes.raizes_api.aplicacao.servico.PedidoServico;
import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.StatusPedido;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public PedidoResposta criar(@Valid @RequestBody CriarPedidoRequisicao requisicao) {
        return pedidoServico.criar(requisicao);
    }

    @GetMapping
    public List<PedidoResposta> listar(
            @RequestParam(required = false) CanalPedido canalPedido,
            @RequestParam(required = false) StatusPedido status
    ) {
        return pedidoServico.listar(canalPedido, status);
    }

    @GetMapping("/{id}")
    public PedidoResposta buscarPorId(@PathVariable UUID id) {
        return pedidoServico.buscarPorId(id);
    }
}
