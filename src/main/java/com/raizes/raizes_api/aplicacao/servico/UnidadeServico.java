package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarUnidadeRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CriarUnidadeRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UnidadeResposta;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UnidadeEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UnidadeJpaRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UnidadeServico {

    private final UnidadeJpaRepositorio unidadeRepositorio;

    public UnidadeServico(UnidadeJpaRepositorio unidadeRepositorio) {
        this.unidadeRepositorio = unidadeRepositorio;
    }

    public UnidadeResposta criar(CriarUnidadeRequisicao requisicao) {
        UnidadeEntidade unidade = new UnidadeEntidade(
                UUID.randomUUID(),
                requisicao.getNome(),
                requisicao.getEndereco(),
                true
        );

        UnidadeEntidade unidadeSalva = unidadeRepositorio.save(unidade);

        return paraResposta(unidadeSalva);
    }

    public List<UnidadeResposta> listar() {
        return unidadeRepositorio.findAll()
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    public UnidadeResposta buscarPorId(UUID id) {
        UnidadeEntidade unidade = unidadeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Unidade nao encontrada."));

        return paraResposta(unidade);
    }

    public UnidadeResposta atualizar(UUID id, AtualizarUnidadeRequisicao requisicao) {
        unidadeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Unidade nao encontrada."));

        UnidadeEntidade unidadeAtualizada = new UnidadeEntidade(
                id,
                requisicao.getNome(),
                requisicao.getEndereco(),
                requisicao.getAtiva()
        );

        UnidadeEntidade unidadeSalva = unidadeRepositorio.save(unidadeAtualizada);

        return paraResposta(unidadeSalva);
    }

    public void desativar(UUID id) {
        UnidadeEntidade unidade = unidadeRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Unidade nao encontrada."));

        UnidadeEntidade unidadeDesativada = new UnidadeEntidade(
                unidade.getId(),
                unidade.getNome(),
                unidade.getEndereco(),
                false
        );

        unidadeRepositorio.save(unidadeDesativada);
    }

    private UnidadeResposta paraResposta(UnidadeEntidade unidade) {
        return new UnidadeResposta(
                unidade.getId(),
                unidade.getNome(),
                unidade.getEndereco(),
                unidade.isAtiva()
        );
    }
}
