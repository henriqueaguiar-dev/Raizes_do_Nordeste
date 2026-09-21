package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.MovimentarEstoqueRequisicao;
import com.raizes.raizes_api.api.dto.resposta.EstoqueResposta;
import com.raizes.raizes_api.dominio.excecao.EstoqueInsuficienteExcecao;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.EstoqueEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.EstoqueJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.ProdutoJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UnidadeJpaRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EstoqueServico {

    private final EstoqueJpaRepositorio estoqueRepositorio;
    private final UnidadeJpaRepositorio unidadeRepositorio;
    private final ProdutoJpaRepositorio produtoRepositorio;

    public EstoqueServico(EstoqueJpaRepositorio estoqueRepositorio,
                          UnidadeJpaRepositorio unidadeRepositorio,
                          ProdutoJpaRepositorio produtoRepositorio) {
        this.estoqueRepositorio = estoqueRepositorio;
        this.unidadeRepositorio = unidadeRepositorio;
        this.produtoRepositorio = produtoRepositorio;
    }

    public EstoqueResposta registrarEntrada(MovimentarEstoqueRequisicao requisicao) {
        validarUnidadeEProduto(requisicao.getUnidadeId(), requisicao.getProdutoId());

        EstoqueEntidade estoqueAtual = estoqueRepositorio
                .findByUnidadeIdAndProdutoId(requisicao.getUnidadeId(), requisicao.getProdutoId())
                .orElse(null);

        EstoqueEntidade estoqueAtualizado;

        if (estoqueAtual == null) {
            estoqueAtualizado = new EstoqueEntidade(
                    UUID.randomUUID(),
                    requisicao.getUnidadeId(),
                    requisicao.getProdutoId(),
                    requisicao.getQuantidade()
            );
        } else {
            estoqueAtualizado = new EstoqueEntidade(
                    estoqueAtual.getId(),
                    estoqueAtual.getUnidadeId(),
                    estoqueAtual.getProdutoId(),
                    estoqueAtual.getQuantidadeDisponivel() + requisicao.getQuantidade()
            );
        }

        return paraResposta(estoqueRepositorio.save(estoqueAtualizado));
    }

    public EstoqueResposta registrarSaida(MovimentarEstoqueRequisicao requisicao) {
        validarUnidadeEProduto(requisicao.getUnidadeId(), requisicao.getProdutoId());

        EstoqueEntidade estoqueAtual = estoqueRepositorio
                .findByUnidadeIdAndProdutoId(requisicao.getUnidadeId(), requisicao.getProdutoId())
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Estoque nao encontrado para esta unidade e produto."));

        if (estoqueAtual.getQuantidadeDisponivel() < requisicao.getQuantidade()) {
            throw new EstoqueInsuficienteExcecao("Estoque insuficiente para realizar a saida.");
        }

        EstoqueEntidade estoqueAtualizado = new EstoqueEntidade(
                estoqueAtual.getId(),
                estoqueAtual.getUnidadeId(),
                estoqueAtual.getProdutoId(),
                estoqueAtual.getQuantidadeDisponivel() - requisicao.getQuantidade()
        );

        return paraResposta(estoqueRepositorio.save(estoqueAtualizado));
    }

    public List<EstoqueResposta> listarPorUnidade(UUID unidadeId) {
        if (!unidadeRepositorio.existsById(unidadeId)) {
            throw new RecursoNaoEncontradoExcecao("Unidade nao encontrada.");
        }

        return estoqueRepositorio.findByUnidadeId(unidadeId)
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    public EstoqueResposta consultarPorUnidadeEProduto(UUID unidadeId, UUID produtoId) {
        EstoqueEntidade estoque = estoqueRepositorio.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Estoque nao encontrado."));

        return paraResposta(estoque);
    }

    private void validarUnidadeEProduto(UUID unidadeId, UUID produtoId) {
        if (!unidadeRepositorio.existsById(unidadeId)) {
            throw new RecursoNaoEncontradoExcecao("Unidade nao encontrada.");
        }

        if (!produtoRepositorio.existsById(produtoId)) {
            throw new RecursoNaoEncontradoExcecao("Produto nao encontrado.");
        }
    }

    private EstoqueResposta paraResposta(EstoqueEntidade estoque) {
        return new EstoqueResposta(
                estoque.getId(),
                estoque.getUnidadeId(),
                estoque.getProdutoId(),
                estoque.getQuantidadeDisponivel()
        );
    }
}