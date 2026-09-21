package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.MovimentarEstoqueRequisicao;
import com.raizes.raizes_api.api.dto.resposta.EstoqueResposta;
import com.raizes.raizes_api.aplicacao.servico.EstoqueServico;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/estoques")
public class EstoqueControlador {

    private final EstoqueServico estoqueServico;

    public EstoqueControlador(EstoqueServico estoqueServico) {
        this.estoqueServico = estoqueServico;
    }

    @PostMapping("/entradas")
    public EstoqueResposta registrarEntrada(@Valid @RequestBody MovimentarEstoqueRequisicao requisicao) {
        return estoqueServico.registrarEntrada(requisicao);
    }

    @PostMapping("/saidas")
    public EstoqueResposta registrarSaida(@Valid @RequestBody MovimentarEstoqueRequisicao requisicao) {
        return estoqueServico.registrarSaida(requisicao);
    }

    @GetMapping("/unidades/{unidadeId}")
    public List<EstoqueResposta> listarPorUnidade(@PathVariable UUID unidadeId) {
        return estoqueServico.listarPorUnidade(unidadeId);
    }

    @GetMapping("/unidades/{unidadeId}/produtos/{produtoId}")
    public EstoqueResposta consultarPorUnidadeEProduto(
            @PathVariable UUID unidadeId,
            @PathVariable UUID produtoId
    ) {
        return estoqueServico.consultarPorUnidadeEProduto(unidadeId, produtoId);
    }
}