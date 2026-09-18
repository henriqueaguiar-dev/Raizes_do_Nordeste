package com.raizes.raizes_api.infraestrutura.persistencia.mapeador;

import com.raizes.raizes_api.dominio.modelo.Pagamento;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PagamentoEntidade;

import java.time.OffsetDateTime;

public class PagamentoMapeador {

    private PagamentoMapeador() {
    }

    public static Pagamento paraDominio(PagamentoEntidade entidade) {
        Pagamento pagamento = new Pagamento(
                entidade.getId(),
                entidade.getPedidoId(),
                entidade.getMetodo(),
                entidade.getValor(),
                entidade.getCodigoTransacaoMock(),
                entidade.getRespostaMock(),
                entidade.getCriadoEm()
        );

        switch (entidade.getStatus()) {
            case APROVADO -> pagamento.aprovar(entidade.getCodigoTransacaoMock());
            case RECUSADO -> pagamento.recusar(entidade.getCodigoTransacaoMock());
            case PENDENTE -> {
            }
        }

        return pagamento;
    }

    public static PagamentoEntidade paraEntidade(Pagamento pagamento) {
        return new PagamentoEntidade(
                pagamento.getId(),
                pagamento.getPedidoId(),
                pagamento.getStatus(),
                pagamento.getMetodo(),
                pagamento.getValor(),
                pagamento.getCodigoTransacaoMock(),
                OffsetDateTime.now()
        );
    }
}
