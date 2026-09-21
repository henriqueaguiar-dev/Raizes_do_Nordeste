package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarProdutoRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CriarProdutoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.ProdutoResposta;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.ProdutoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.ProdutoJpaRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProdutoServico {

    private final ProdutoJpaRepositorio produtoRepositorio;

    public ProdutoServico(ProdutoJpaRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
    }

    public ProdutoResposta criar(CriarProdutoRequisicao requisicao) {
        ProdutoEntidade produto = new ProdutoEntidade(
                UUID.randomUUID(),
                requisicao.getNome(),
                requisicao.getDescricao(),
                requisicao.getPreco(),
                true
        );

        ProdutoEntidade produtoSalvo = produtoRepositorio.save(produto);

        return paraResposta(produtoSalvo);
    }

    public List<ProdutoResposta> listar() {
        return produtoRepositorio.findAll()
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    public ProdutoResposta buscarPorId(UUID id) {
        ProdutoEntidade produto = produtoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Produto nao encontrado."));

        return paraResposta(produto);
    }

    public ProdutoResposta atualizar(UUID id, AtualizarProdutoRequisicao requisicao) {
        produtoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Produto nao encontrado."));

        ProdutoEntidade produtoAtualizado = new ProdutoEntidade(
                id,
                requisicao.getNome(),
                requisicao.getDescricao(),
                requisicao.getPreco(),
                requisicao.getAtivo()
        );

        ProdutoEntidade produtoSalvo = produtoRepositorio.save(produtoAtualizado);

        return paraResposta(produtoSalvo);
    }

    private ProdutoResposta paraResposta(ProdutoEntidade produto) {
        return new ProdutoResposta(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.isAtivo()
        );
    }

    public void desativar(UUID id) {
    ProdutoEntidade produto = produtoRepositorio.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Produto nao encontrado."));

    ProdutoEntidade produtoDesativado = new ProdutoEntidade(
            produto.getId(),
            produto.getNome(),
            produto.getDescricao(),
            produto.getPreco(),
            false
    );

    produtoRepositorio.save(produtoDesativado);
}
}