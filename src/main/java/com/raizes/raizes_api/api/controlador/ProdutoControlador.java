package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarProdutoRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CriarProdutoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.ProdutoResposta;
import com.raizes.raizes_api.aplicacao.servico.ProdutoServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/produtos")
public class ProdutoControlador {

    private final ProdutoServico produtoServico;

    public ProdutoControlador(ProdutoServico produtoServico) {
        this.produtoServico = produtoServico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResposta criar(@Valid @RequestBody CriarProdutoRequisicao requisicao) {
        return produtoServico.criar(requisicao);
    }

    @GetMapping
    public List<ProdutoResposta> listar() {
        return produtoServico.listar();
    }

    @GetMapping("/{id}")
    public ProdutoResposta buscarPorId(@PathVariable UUID id) {
        return produtoServico.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public ProdutoResposta atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarProdutoRequisicao requisicao
    ) {
        return produtoServico.atualizar(id, requisicao);
    }
}
