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
    @Autowired private com.raizes.raizes_api.infraestrutura.persistencia.repositorio.FidelidadeCreditoJpaRepositorio creditos;
    @Autowired private com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UsuarioJpaRepositorio usuarios;

    private UUID criarUsuario(boolean consentimento) {
        UUID id = UUID.randomUUID();
        usuarios.save(new com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade(
                id, "Cliente", id+"@teste.local", "hash", com.raizes.raizes_api.dominio.enums.PerfilUsuario.CLIENTE,
                true, consentimento, OffsetDateTime.now()));
        return id;
    }

    @Test
    void somaAprovadosAcumulaCentavosEIgnoraRecusadosEOutrosClientes() {
        UUID clienteId = criarUsuario(true);
        registrar(clienteId, "10.50", StatusPagamento.APROVADO);
        registrar(clienteId, "9.50", StatusPagamento.APROVADO);
        registrar(clienteId, "100.00", StatusPagamento.RECUSADO);
        registrar(criarUsuario(true), "500.00", StatusPagamento.APROVADO);

        var saldo = fidelidadeServico.consultarSaldo(clienteId);
        assertEquals(clienteId, saldo.clienteId());
        assertEquals(20, saldo.pontos());
        assertEquals(20, fidelidadeServico.consultarSaldo(clienteId).pontos());
    }

    @Test
    void retornaZeroSemPagamentosAprovados() {
        UUID clienteId = criarUsuario(true);
        assertEquals(0, fidelidadeServico.consultarSaldo(clienteId).pontos());
        registrar(clienteId, "50.00", StatusPagamento.RECUSADO);
        assertEquals(0, fidelidadeServico.consultarSaldo(clienteId).pontos());
    }

    @Test
    void arredondaSaldoParaBaixo() {
        UUID clienteId = criarUsuario(true);
        registrar(clienteId, "10.90", StatusPagamento.APROVADO);
        assertEquals(10, fidelidadeServico.consultarSaldo(clienteId).pontos());
    }

    @Test
    void semConsentimentoNaoCreditaNemPermiteResgate() {
        UUID id = criarUsuario(false);
        registrar(id, "200.00", StatusPagamento.APROVADO);
        assertEquals(0, fidelidadeServico.consultarSaldo(id).pontos());
        org.junit.jupiter.api.Assertions.assertThrows(com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao.class,
                () -> fidelidadeServico.resgatar(id));
    }

    @Test
    void resgateConsomePontosEPreservaHistorico() {
        UUID id = criarUsuario(true);
        creditos.save(new com.raizes.raizes_api.infraestrutura.persistencia.entidade.FidelidadeCreditoEntidade(
                id, UUID.randomUUID(), new BigDecimal("250.50")));
        var primeiro = fidelidadeServico.resgatar(id);
        assertEquals(100, primeiro.pontosConsumidos());
        assertEquals("CREDITO_PAGAMENTO", primeiro.beneficio());
        assertEquals("FID-"+primeiro.id(), primeiro.codigoResgate());
        assertEquals(new BigDecimal("1.00"), primeiro.valorCredito());
        assertEquals(new BigDecimal("1.00"), fidelidadeServico.consultarSaldo(id).saldoCredito());
        assertEquals(150, fidelidadeServico.consultarSaldo(id).pontos());
        fidelidadeServico.resgatar(id);
        assertEquals(50, fidelidadeServico.consultarSaldo(id).pontos());
        assertEquals(2, fidelidadeServico.listarResgates(id).size());
        org.junit.jupiter.api.Assertions.assertThrows(com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao.class,
                () -> fidelidadeServico.resgatar(id));
        assertEquals(50, fidelidadeServico.consultarSaldo(id).pontos());
    }

    @Test
    void creditoDoMesmoPagamentoNaoDuplica() {
        UUID id = criarUsuario(true);
        registrar(id, "100.00", StatusPagamento.APROVADO);
        var pagamento = pagamentos.findAll().stream().filter(p -> p.getValor().compareTo(new BigDecimal("100.00")) == 0).findFirst().orElseThrow();
        fidelidadeServico.registrarCredito(id, pagamento);
        assertEquals(0, fidelidadeServico.consultarSaldo(id).pontos());
        assertEquals(new BigDecimal("1.00"), fidelidadeServico.consultarSaldo(id).saldoCredito());
    }

    @Test
    void converteTodosOsBlocosDeCemPreservandoPontosRestantes() {
        UUID id = criarUsuario(true);
        registrar(id, "250.50", StatusPagamento.APROVADO);
        assertEquals(50, fidelidadeServico.consultarSaldo(id).pontos());
        assertEquals(new BigDecimal("2.00"), fidelidadeServico.consultarSaldo(id).saldoCredito());
        var conversao = fidelidadeServico.listarResgates(id).getFirst();
        assertEquals(200, conversao.pontosConsumidos());
        assertEquals(new BigDecimal("2.00"), conversao.valorCredito());
    }

    @Test
    void detalhaGanhosConversaoERestante() {
        UUID id = criarUsuario(true);
        registrar(id, "675.00", StatusPagamento.APROVADO);
        var saldo = fidelidadeServico.consultarSaldo(id);
        assertEquals(675, saldo.pontosGanhos());
        assertEquals(600, saldo.pontosConvertidos());
        assertEquals(new BigDecimal("6.00"), saldo.creditoConvertido());
        assertEquals(75, saldo.restante());
        assertEquals(new BigDecimal("6.00"), saldo.saldoCredito());
    }

    @Test
    void estornoRecalculaConversaoQuandoCentavosDeComprasDiferentesCompletaramCem() {
        UUID id=criarUsuario(true);
        registrar(id,"99.50",StatusPagamento.APROVADO);
        var origem=pagamentos.findAll().stream().filter(p -> p.getValor().compareTo(new BigDecimal("99.50"))==0).findFirst().orElseThrow();
        registrar(id,"0.50",StatusPagamento.APROVADO);
        assertEquals(new BigDecimal("1.00"),fidelidadeServico.consultarSaldo(id).saldoCredito());
        fidelidadeServico.estornarPagamento(id,origem.getId());
        assertEquals(0,fidelidadeServico.consultarSaldo(id).pontosGanhos());
        assertEquals(0,fidelidadeServico.consultarSaldo(id).restante());
        assertEquals(new BigDecimal("0.00"),fidelidadeServico.consultarSaldo(id).saldoCredito());
        fidelidadeServico.estornarPagamento(id,origem.getId());
        assertEquals(new BigDecimal("0.00"),fidelidadeServico.consultarSaldo(id).saldoCredito());
    }

    private void registrar(UUID clienteId, String valor, StatusPagamento status) {
        UUID pedidoId = UUID.randomUUID();
        OffsetDateTime agora = OffsetDateTime.now();
        BigDecimal total = new BigDecimal(valor);
        pedidos.save(new PedidoEntidade(pedidoId, clienteId, UUID.randomUUID(), CanalPedido.APP,
                status == StatusPagamento.APROVADO ? StatusPedido.PAGO : StatusPedido.AGUARDANDO_PAGAMENTO,
                total, agora, agora));
        var pagamento = pagamentos.save(new PagamentoEntidade(UUID.randomUUID(), pedidoId, status, MetodoPagamento.MOCK,
                total, "MOCK-" + UUID.randomUUID(), "Teste", agora));
        fidelidadeServico.registrarCredito(clienteId, pagamento);
    }
}
