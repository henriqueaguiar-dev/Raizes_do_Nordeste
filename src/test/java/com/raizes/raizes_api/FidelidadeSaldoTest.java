package com.raizes.raizes_api;

import com.raizes.raizes_api.aplicacao.servico.FidelidadeServico;
import com.raizes.raizes_api.dominio.enums.CanalPedido;
import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import com.raizes.raizes_api.dominio.enums.StatusPagamento;
import com.raizes.raizes_api.dominio.enums.StatusPedido;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PagamentoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PedidoEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PagamentoJpaRepositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PedidoJpaRepositorio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(FidelidadeServico.class)
class FidelidadeSaldoTest {
    @Autowired private FidelidadeServico fidelidadeServico;
    @Autowired private PagamentoJpaRepositorio pagamentos;
    @Autowired private PedidoJpaRepositorio pedidos;

    @Test
    void somaAprovadosAcumulaCentavosEIgnoraRecusadosEOutrosClientes() {
        UUID clienteId = UUID.randomUUID();
        registrar(clienteId, "10.50", StatusPagamento.APROVADO);
        registrar(clienteId, "9.50", StatusPagamento.APROVADO);
        registrar(clienteId, "100.00", StatusPagamento.RECUSADO);
        registrar(UUID.randomUUID(), "500.00", StatusPagamento.APROVADO);

        var saldo = fidelidadeServico.consultarSaldo(clienteId);
        assertEquals(clienteId, saldo.clienteId());
        assertEquals(20, saldo.pontos());
        assertEquals(20, fidelidadeServico.consultarSaldo(clienteId).pontos());
    }

    @Test
    void retornaZeroSemPagamentosAprovados() {
        UUID clienteId = UUID.randomUUID();
        assertEquals(0, fidelidadeServico.consultarSaldo(clienteId).pontos());
        registrar(clienteId, "50.00", StatusPagamento.RECUSADO);
        assertEquals(0, fidelidadeServico.consultarSaldo(clienteId).pontos());
    }

    @Test
    void arredondaSaldoParaBaixo() {
        UUID clienteId = UUID.randomUUID();
        registrar(clienteId, "10.90", StatusPagamento.APROVADO);
        assertEquals(10, fidelidadeServico.consultarSaldo(clienteId).pontos());
    }

    private void registrar(UUID clienteId, String valor, StatusPagamento status) {
        UUID pedidoId = UUID.randomUUID();
        OffsetDateTime agora = OffsetDateTime.now();
        BigDecimal total = new BigDecimal(valor);
        pedidos.save(new PedidoEntidade(pedidoId, clienteId, UUID.randomUUID(), CanalPedido.APP,
                status == StatusPagamento.APROVADO ? StatusPedido.PAGO : StatusPedido.AGUARDANDO_PAGAMENTO,
                total, agora, agora));
        pagamentos.save(new PagamentoEntidade(UUID.randomUUID(), pedidoId, status, MetodoPagamento.MOCK,
                total, "MOCK-" + UUID.randomUUID(), "Teste", agora));
    }
}
