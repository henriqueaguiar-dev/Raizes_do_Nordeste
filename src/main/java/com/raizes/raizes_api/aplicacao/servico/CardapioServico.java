package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarCardapioRequisicao;
import com.raizes.raizes_api.api.dto.resposta.CardapioItemResposta;
import com.raizes.raizes_api.api.dto.resposta.CardapioVinculoResposta;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.ProdutoUnidadeEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.ProdutoJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.ProdutoUnidadeJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UnidadeJpaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class CardapioServico {
    private final ProdutoUnidadeJpaRepositorio cardapioRepositorio;
    private final UnidadeJpaRepositorio unidadeRepositorio;
    private final ProdutoJpaRepositorio produtoRepositorio;

    public CardapioServico(ProdutoUnidadeJpaRepositorio cardapioRepositorio,
                          UnidadeJpaRepositorio unidadeRepositorio, ProdutoJpaRepositorio produtoRepositorio) {
        this.cardapioRepositorio = cardapioRepositorio;
        this.unidadeRepositorio = unidadeRepositorio;
        this.produtoRepositorio = produtoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<CardapioItemResposta> consultar(UUID unidadeId) {
        unidadeRepositorio.findById(unidadeId)
                .filter(u -> Boolean.TRUE.equals(u.isAtiva()))
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Unidade inexistente ou inativa."));
        return cardapioRepositorio.consultarCardapio(unidadeId);
    }

    @Transactional
    public CardapioVinculoResposta atualizar(UUID unidadeId, UUID produtoId, AtualizarCardapioRequisicao requisicao) {
        // Mesmo bloqueio usado na criacao de pedidos: a oferta nao muda durante uma venda.
        unidadeRepositorio.buscarComBloqueio(unidadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Unidade nao encontrada."));
        if (!produtoRepositorio.existsById(produtoId)) {
            throw new RecursoNaoEncontradoExcecao("Produto nao encontrado.");
        }
        ProdutoUnidadeEntidade vinculo = cardapioRepositorio.findByUnidadeIdAndProdutoId(unidadeId, produtoId)
                .orElseGet(() -> new ProdutoUnidadeEntidade(UUID.randomUUID(), unidadeId, produtoId, false));
        vinculo.alterarAtivo(requisicao.ativo());
        cardapioRepositorio.save(vinculo);
        return new CardapioVinculoResposta(unidadeId, produtoId, vinculo.isAtivo());
    }
}
