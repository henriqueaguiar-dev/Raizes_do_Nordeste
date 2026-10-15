package com.raizes.raizes_api.infraestrutura.pagamento;

import com.raizes.raizes_api.dominio.pagamento.*;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;
import com.raizes.raizes_api.dominio.excecao.ProvedorPagamentoExcecao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

@Component
public class ProvedorPagamentoMockHttp implements ProvedorPagamento {
    private final URI endpoint;
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3)).build();

    public ProvedorPagamentoMockHttp(@Value("${app.pagamento.mock.url}") String url, ObjectMapper mapper) {
        this.endpoint = URI.create(url);
        this.mapper = mapper;
    }

    @Override
    public ResultadoProvedorPagamento solicitar(SolicitacaoPagamentoExterno solicitacao) {
        try {
            HttpRequest request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", solicitacao.pedidoId()+":"+solicitacao.numeroTentativa())
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(solicitacao))).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 && response.statusCode() != 201) {
                throw new ProvedorPagamentoExcecao("Servico de pagamento mock retornou erro HTTP.");
            }
            RetornoPagamentoExterno payload = mapper.readValue(response.body(), RetornoPagamentoExterno.class);
            if (payload == null || !solicitacao.pedidoId().equals(payload.pedidoId())
                    || payload.valor() == null || solicitacao.valor().compareTo(payload.valor()) != 0
                    || !solicitacao.moeda().equals(payload.moeda())
                    || (payload.status() != StatusPagamento.APROVADO && payload.status() != StatusPagamento.RECUSADO)
                    || payload.codigoTransacao() == null || payload.codigoTransacao().isBlank()
                    || payload.codigoTransacao().length() > 120
                    || payload.mensagem() == null || payload.mensagem().length() > 1000) {
                throw new ProvedorPagamentoExcecao("Resposta inconsistente do servico de pagamento mock.");
            }
            return new ResultadoProvedorPagamento(response.statusCode(), payload);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProvedorPagamentoExcecao("Solicitacao de pagamento interrompida.", e);
        } catch (ProvedorPagamentoExcecao e) {
            throw e;
        } catch (Exception e) {
            throw new ProvedorPagamentoExcecao("Servico de pagamento mock indisponivel ou resposta invalida.", e);
        }
    }

    @Override
    public ResultadoEstorno estornar(SolicitacaoEstornoExterno solicitacao) {
        try {
            HttpRequest request=HttpRequest.newBuilder(endpoint.resolve("estornos")).timeout(Duration.ofSeconds(5))
                    .header("Content-Type","application/json")
                    .header("Idempotency-Key",solicitacao.pagamentoId()+":estorno")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(solicitacao))).build();
            var response=client.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=200 && response.statusCode()!=201)
                throw new ProvedorPagamentoExcecao("Servico mock recusou o estorno.");
            var payload=mapper.readValue(response.body(),RetornoEstornoExterno.class);
            if(payload==null || !solicitacao.pagamentoId().equals(payload.pagamentoId())
                    || payload.status()!=StatusPagamento.ESTORNADO || payload.valor()==null
                    || solicitacao.valor().compareTo(payload.valor())!=0 || !solicitacao.moeda().equals(payload.moeda())
                    || payload.codigoEstorno()==null || payload.codigoEstorno().isBlank()
                    || payload.mensagem()==null)
                throw new ProvedorPagamentoExcecao("Resposta inconsistente do estorno mock.");
            return new ResultadoEstorno(response.statusCode(),payload);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();throw new ProvedorPagamentoExcecao("Estorno interrompido.",e);
        } catch(ProvedorPagamentoExcecao e) { throw e;
        } catch(Exception e) { throw new ProvedorPagamentoExcecao("Servico de estorno mock indisponivel.",e); }
    }
}
