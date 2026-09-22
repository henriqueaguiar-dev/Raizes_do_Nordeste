package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.CriarPedidoItemRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CriarPedidoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.PedidoItemResposta;
import com.raizes.raizes_api.api.dto.resposta.PedidoResposta;
import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.StatusPedido;
import com.raizes.raizes_api.dominio.excecao.EstoqueInsuficienteExcecao;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.dominio.excecao.StatusPedidoInvalidoExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.EstoqueEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoItemEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.ProdutoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.EstoqueJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PedidoJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.ProdutoJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UnidadeJpaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PedidoServico {

    private final PedidoJpaRepositorio pedidoRepositorio;
    private final UnidadeJpaRepositorio unidadeRepositorio;
    private final ProdutoJpaRepositorio produtoRepositorio;
    private final EstoqueJpaRepositorio estoqueRepositorio;

    public PedidoServico(PedidoJpaRepositorio pedidoRepositorio,
            UnidadeJpaRepositorio unidadeRepositorio,
            ProdutoJpaRepositorio produtoRepositorio,
            EstoqueJpaRepositorio estoqueRepositorio) {
        this.pedidoRepositorio = pedidoRepositorio;
        this.unidadeRepositorio = unidadeRepositorio;
        this.produtoRepositorio = produtoRepositorio;
        this.estoqueRepositorio = estoqueRepositorio;
    }

    @Transactional
    public PedidoResposta criar(CriarPedidoRequisicao requisicao) {
        if (!unidadeRepositorio.existsById(requisicao.getUnidadeId())) {
            throw new RecursoNaoEncontradoExcecao("Unidade nao encontrada.");
        }

        UUID pedidoId = UUID.randomUUID();
        List<PedidoItemEntidade> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CriarPedidoItemRequisicao itemRequisicao : requisicao.getItens()) {
            ProdutoEntidade produto = produtoRepositorio.findById(itemRequisicao.getProdutoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Produto nao encontrado."));

            if (!produto.isAtivo()) {
                throw new RegraDeNegocioExcecao("Produto inativo nao pode ser vendido.");
            }

            EstoqueEntidade estoque = estoqueRepositorio
                    .findByUnidadeIdAndProdutoId(requisicao.getUnidadeId(), itemRequisicao.getProdutoId())
                    .orElseThrow(
                            () -> new RecursoNaoEncontradoExcecao("Estoque nao encontrado para o produto informado."));

            if (estoque.getQuantidadeDisponivel() < itemRequisicao.getQuantidade()) {
                throw new EstoqueInsuficienteExcecao("Estoque insuficiente para o produto informado.");
            }

            EstoqueEntidade estoqueAtualizado = new EstoqueEntidade(
                    estoque.getId(),
                    estoque.getUnidadeId(),
                    estoque.getProdutoId(),
                    estoque.getQuantidadeDisponivel() - itemRequisicao.getQuantidade());

            estoqueRepositorio.save(estoqueAtualizado);

            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(itemRequisicao.getQuantidade()));
            total = total.add(subtotal);

            PedidoItemEntidade item = new PedidoItemEntidade(
                    UUID.randomUUID(),
                    produto.getId(),
                    itemRequisicao.getQuantidade(),
                    produto.getPreco(),
                    subtotal);

            itens.add(item);
        }

        OffsetDateTime agora = OffsetDateTime.now();

        PedidoEntidade pedido = new PedidoEntidade(
                pedidoId,
                requisicao.getClienteId(),
                requisicao.getUnidadeId(),
                requisicao.getCanalPedido(),
                StatusPedido.AGUARDANDO_PAGAMENTO,
                total,
                agora,
                agora);

        itens.forEach(pedido::adicionarItem);

        PedidoEntidade pedidoSalvo = pedidoRepositorio.save(pedido);

        return paraResposta(pedidoSalvo);
    }

    public List<PedidoResposta> listar(CanalPedido canalPedido, StatusPedido status) {
        List<PedidoEntidade> pedidos;

        if (canalPedido != null && status != null) {
            pedidos = pedidoRepositorio.findByCanalPedidoAndStatus(canalPedido, status);
        } else if (canalPedido != null) {
            pedidos = pedidoRepositorio.findByCanalPedido(canalPedido);
        } else if (status != null) {
            pedidos = pedidoRepositorio.findByStatus(status);
        } else {
            pedidos = pedidoRepositorio.findAll();
        }

        return pedidos.stream()
                .map(this::paraResposta)
                .toList();
    }

    public PedidoResposta buscarPorId(UUID id) {
        PedidoEntidade pedido = pedidoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Pedido nao encontrado."));

        return paraResposta(pedido);
    }

    private PedidoResposta paraResposta(PedidoEntidade pedido) {
        List<PedidoItemResposta> itens = pedido.getItens()
                .stream()
                .map(item -> new PedidoItemResposta(
                        item.getProdutoId(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getSubtotal()))
                .toList();

        return new PedidoResposta(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getUnidadeId(),
                pedido.getCanalPedido(),
                pedido.getStatus(),
                pedido.getTotal(),
                itens);
    }

    @Transactional
    public PedidoResposta atualizarStatus(UUID id, StatusPedido novoStatus) {
        PedidoEntidade pedido = pedidoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Pedido nao encontrado."));

        validarMudancaStatus(pedido.getStatus(), novoStatus);

        PedidoEntidade pedidoAtualizado = new PedidoEntidade(
                pedido.getId(),
                pedido.getClienteId(),
                pedido.getUnidadeId(),
                pedido.getCanalPedido(),
                novoStatus,
                pedido.getTotal(),
                pedido.getCriadoEm(),
                OffsetDateTime.now());

        pedido.getItens().forEach(pedidoAtualizado::adicionarItem);

        PedidoEntidade pedidoSalvo = pedidoRepositorio.save(pedidoAtualizado);

        return paraResposta(pedidoSalvo);
    }

    private void validarMudancaStatus(StatusPedido statusAtual, StatusPedido novoStatus) {
        if (statusAtual == StatusPedido.CANCELADO || statusAtual == StatusPedido.ENTREGUE) {
            throw new StatusPedidoInvalidoExcecao("Pedido finalizado nao pode mudar de status.");
        }

        if (novoStatus == StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new StatusPedidoInvalidoExcecao("Nao e permitido voltar para aguardando pagamento.");
        }

        if (statusAtual == StatusPedido.AGUARDANDO_PAGAMENTO && novoStatus != StatusPedido.CANCELADO) {
            throw new StatusPedidoInvalidoExcecao("Pedido aguardando pagamento so pode ser cancelado.");
        }

        if (statusAtual == StatusPedido.PAGO && novoStatus != StatusPedido.EM_PREPARO
                && novoStatus != StatusPedido.CANCELADO) {
            throw new StatusPedidoInvalidoExcecao("Pedido pago deve ir para preparo ou ser cancelado.");
        }

        if (statusAtual == StatusPedido.EM_PREPARO && novoStatus != StatusPedido.PRONTO
                && novoStatus != StatusPedido.CANCELADO) {
            throw new StatusPedidoInvalidoExcecao("Pedido em preparo deve ir para pronto ou ser cancelado.");
        }

        if (statusAtual == StatusPedido.PRONTO && novoStatus != StatusPedido.ENTREGUE
                && novoStatus != StatusPedido.CANCELADO) {
            throw new StatusPedidoInvalidoExcecao("Pedido pronto deve ser entregue ou cancelado.");
        }
    }
}