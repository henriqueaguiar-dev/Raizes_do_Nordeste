package com.raizes.raizes_api;

import com.raizes.raizes_api.dominio.pagamento.*;
import com.raizes.raizes_api.dominio.enums.*;
import com.raizes.raizes_api.dominio.excecao.ProvedorPagamentoExcecao;
import com.raizes.raizes_api.infraestrutura.pagamento.ProvedorPagamentoMockHttp;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;
import java.net.InetSocketAddress;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ProvedorPagamentoMockHttpTest {
    HttpServer servidor;
    ProvedorPagamentoMockHttp provedor;
    ObjectMapper mapper = new ObjectMapper();
    int statusHttp = 201;
    String resposta;
    SolicitacaoPagamentoExterno solicitacao = new SolicitacaoPagamentoExterno(UUID.randomUUID(),
            CanalPedido.TOTEM, MetodoPagamento.MOCK, new BigDecimal("12.34"), "BRL", true);

    @BeforeEach void iniciar() throws Exception {
        resposta = mapper.writeValueAsString(new RetornoPagamentoExterno(solicitacao.pedidoId(), "MOCK-teste",
                StatusPagamento.RECUSADO, solicitacao.valor(), "BRL", "Recusado pelo provedor"));
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        servidor.createContext("/pagamentos", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] bytes = resposta.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusHttp,bytes.length);
            try(var out=exchange.getResponseBody()){out.write(bytes);}
        });
        servidor.start();
        provedor = new ProvedorPagamentoMockHttp("http://127.0.0.1:"+servidor.getAddress().getPort()+"/pagamentos",mapper);
    }
    @AfterEach void encerrar() { servidor.stop(0); }

    @Test void respeitaStatusDoProvedorMesmoQuandoSimulacaoSolicitaAprovacao() {
        var resultado = provedor.solicitar(solicitacao);
        assertEquals(201,resultado.statusHttp());
        assertEquals(StatusPagamento.RECUSADO,resultado.payload().status());
    }
    @Test void rejeitaValorOuPedidoDivergente() {
        resposta = mapper.writeValueAsString(new RetornoPagamentoExterno(UUID.randomUUID(), "MOCK-teste",
                StatusPagamento.APROVADO, new BigDecimal("100.00"), "BRL", "Aprovado"));
        assertThrows(ProvedorPagamentoExcecao.class,()->provedor.solicitar(solicitacao));
    }
    @Test void rejeitaRespostaInvalida() {
        resposta = "{invalido";
        assertThrows(ProvedorPagamentoExcecao.class,()->provedor.solicitar(solicitacao));
    }
    @Test void trataFalhaHttpEConexaoEncerrada() {
        statusHttp = 503;
        assertThrows(ProvedorPagamentoExcecao.class,()->provedor.solicitar(solicitacao));
        servidor.stop(0);
        assertThrows(ProvedorPagamentoExcecao.class,()->provedor.solicitar(solicitacao));
    }
}
