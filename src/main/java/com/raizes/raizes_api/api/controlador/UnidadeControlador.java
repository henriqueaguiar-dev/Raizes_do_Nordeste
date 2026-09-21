package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarUnidadeRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CriarUnidadeRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UnidadeResposta;
import com.raizes.raizes_api.aplicacao.servico.UnidadeServico;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/unidades")
public class UnidadeControlador {

    private final UnidadeServico unidadeServico;

    public UnidadeControlador(UnidadeServico unidadeServico) {
        this.unidadeServico = unidadeServico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnidadeResposta criar(@Valid @RequestBody CriarUnidadeRequisicao requisicao) {
        return unidadeServico.criar(requisicao);
    }

    @GetMapping
    public List<UnidadeResposta> listar() {
        return unidadeServico.listar();
    }

    @GetMapping("/{id}")
    public UnidadeResposta buscarPorId(@PathVariable UUID id) {
        return unidadeServico.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public UnidadeResposta atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarUnidadeRequisicao requisicao
    ) {
        return unidadeServico.atualizar(id, requisicao);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable UUID id) {
        unidadeServico.desativar(id);
    }
}