package com.raizes.raizes_api.api.dto.resposta;

import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;

import java.math.BigDecimal;
import java.util.UUID;

public class PagamentoResposta {

    private UUID id;
    private UUID pedidoId;
    private StatusPagamento status;
    private MetodoPagamento metodo;
    private BigDecimal valor;
    private String codigoTransacaoMock;
    private String respostaMock;

    public PagamentoResposta(UUID id, UUID pedidoId, StatusPagamento status, MetodoPagamento metodo,
                             BigDecimal valor, String codigoTransacaoMock, String respostaMock) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.status = status;
        this.metodo = metodo;
        this.valor = valor;
        this.codigoTransacaoMock = codigoTransacaoMock;
        this.respostaMock = respostaMock;
    }

    private BigDecimal valorFidelidade = BigDecimal.ZERO;
    public void definirValorFidelidade(BigDecimal valor) { this.valorFidelidade = valor; }
    public BigDecimal getValorFidelidade() { return valorFidelidade; }
    public BigDecimal getValorExterno() { return valor.subtract(valorFidelidade); }

    private com.raizes.raizes_api.dominio.pagamento.SolicitacaoPagamentoExterno solicitacaoServico;
    private com.raizes.raizes_api.dominio.pagamento.RetornoPagamentoExterno retornoServico;
    private Integer statusHttpServico;
    public void definirIntegracao(com.raizes.raizes_api.dominio.pagamento.SolicitacaoPagamentoExterno solicitacao,
            com.raizes.raizes_api.dominio.pagamento.RetornoPagamentoExterno retorno, Integer statusHttp) {
        this.solicitacaoServico = solicitacao;
        this.retornoServico = retorno;
        this.statusHttpServico = statusHttp;
    }
    public com.raizes.raizes_api.dominio.pagamento.SolicitacaoPagamentoExterno getSolicitacaoServico() { return solicitacaoServico; }
    public com.raizes.raizes_api.dominio.pagamento.RetornoPagamentoExterno getRetornoServico() { return retornoServico; }
    public Integer getStatusHttpServico() { return statusHttpServico; }

    private int numeroTentativa;
    public int getNumeroTentativa() { return numeroTentativa; }
    public void definirNumeroTentativa(int numero) { this.numeroTentativa = numero; }

    private com.raizes.raizes_api.dominio.pagamento.RetornoEstornoExterno estornoServico;
    private Integer statusHttpEstorno;
    public void definirEstorno(com.raizes.raizes_api.dominio.pagamento.RetornoEstornoExterno retorno,Integer statusHttp){
        this.estornoServico=retorno;this.statusHttpEstorno=statusHttp;
    }
    public com.raizes.raizes_api.dominio.pagamento.RetornoEstornoExterno getEstornoServico(){return estornoServico;}
    public Integer getStatusHttpEstorno(){return statusHttpEstorno;}

    public UUID getId() {
        return id;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getCodigoTransacaoMock() {
        return codigoTransacaoMock;
    }

    public String getRespostaMock() {
        return respostaMock;
    }
}
