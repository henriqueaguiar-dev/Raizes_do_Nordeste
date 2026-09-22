package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.ProcessarPagamentoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.PagamentoResposta;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;
import com.raizes.raizes_api.dominio.enums.StatusPedido;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PagamentoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PagamentoJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PedidoJpaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PagamentoServico {

    private final PagamentoJpaRepositorio pagamentoRepositorio;
    private final PedidoJpaRepositorio pedidoRepositorio;

    public PagamentoServico(PagamentoJpaRepositorio pagamentoRepositorio,
                            PedidoJpaRepositorio pedidoRepositorio) {
        this.pagamentoRepositorio = pagamentoRepositorio;
        this.pedidoRepositorio = pedidoRepositorio;
    }

    @Transactional
    public PagamentoResposta processar(UUID pedidoId, ProcessarPagamentoRequisicao requisicao) {
        PedidoEntidade pedido = pedidoRepositorio.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Pedido nao encontrado."));

        if (pagamentoRepositorio.findByPedidoId(pedidoId).isPresent()) {
            throw new RegraDeNegocioExcecao("Pedido ja possui pagamento registrado.");
        }

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new RegraDeNegocioExcecao("Pedido nao esta aguardando pagamento.");
        }

        boolean aprovado = Boolean.TRUE.equals(requisicao.getAprovadoMock());

        StatusPagamento statusPagamento = aprovado
                ? StatusPagamento.APROVADO
                : StatusPagamento.RECUSADO;

        String codigoTransacao = "MOCK-" + UUID.randomUUID();

        String respostaMock = aprovado
                ? "Pagamento mock aprovado."
                : "Pagamento mock recusado.";

        PagamentoEntidade pagamento = new PagamentoEntidade(
                UUID.randomUUID(),
                pedido.getId(),
                statusPagamento,
                requisicao.getMetodo(),
                pedido.getTotal(),
                codigoTransacao,
                respostaMock,
                OffsetDateTime.now()
        );

        PagamentoEntidade pagamentoSalvo = pagamentoRepositorio.save(pagamento);

        if (aprovado) {
            PedidoEntidade pedidoPago = new PedidoEntidade(
                    pedido.getId(),
                    pedido.getClienteId(),
                    pedido.getUnidadeId(),
                    pedido.getCanalPedido(),
                    StatusPedido.PAGO,
                    pedido.getTotal(),
                    pedido.getCriadoEm(),
                    OffsetDateTime.now()
            );

            pedido.getItens().forEach(pedidoPago::adicionarItem);

            pedidoRepositorio.save(pedidoPago);
        }

        return paraResposta(pagamentoSalvo);
    }

    public PagamentoResposta buscarPorPedido(UUID pedidoId) {
        PagamentoEntidade pagamento = pagamentoRepositorio.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Pagamento nao encontrado para este pedido."));

        return paraResposta(pagamento);
    }

    private PagamentoResposta paraResposta(PagamentoEntidade pagamento) {
        return new PagamentoResposta(
                pagamento.getId(),
                pagamento.getPedidoId(),
                pagamento.getStatus(),
                pagamento.getMetodo(),
                pagamento.getValor(),
                pagamento.getCodigoTransacaoMock(),
                pagamento.getRespostaMock()
        );
    }
}