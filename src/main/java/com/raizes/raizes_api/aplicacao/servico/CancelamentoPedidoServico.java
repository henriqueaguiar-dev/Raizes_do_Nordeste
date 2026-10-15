package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.dominio.enums.StatusPedido;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.*;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class CancelamentoPedidoServico {
    private final PedidoJpaRepositorio pedidos;
    private final UnidadeJpaRepositorio unidades;
    private final EstoqueJpaRepositorio estoques;
    private final PerdaEstoqueJpaRepositorio perdas;
    private final PagamentoServico pagamentos;
    private final AuditoriaServico auditoria;
    public CancelamentoPedidoServico(PedidoJpaRepositorio pedidos,UnidadeJpaRepositorio unidades,
            EstoqueJpaRepositorio estoques,PerdaEstoqueJpaRepositorio perdas,PagamentoServico pagamentos,AuditoriaServico auditoria){
        this.pedidos=pedidos;this.unidades=unidades;this.estoques=estoques;this.perdas=perdas;
        this.pagamentos=pagamentos;this.auditoria=auditoria;
    }
    @Transactional
    public void cancelar(PedidoEntidade pedido,UUID autor,String motivo){
        if(pedido.getStatus()==StatusPedido.CANCELADO || pedido.getStatus()==StatusPedido.ENTREGUE)
            throw new RegraDeNegocioExcecao("Pedido finalizado nao pode ser cancelado.");
        unidades.buscarComBloqueio(pedido.getUnidadeId()).orElseThrow();
        boolean repor=pedido.getStatus()==StatusPedido.AGUARDANDO_PAGAMENTO || pedido.getStatus()==StatusPedido.PAGO;
        for(var item:pedido.getItens()) {
            if(repor) {
                var estoque=estoques.findByUnidadeIdAndProdutoId(pedido.getUnidadeId(),item.getProdutoId()).orElseThrow();
                int quantidade;
                try { quantidade=Math.addExact(estoque.getQuantidadeDisponivel(),item.getQuantidade()); }
                catch(ArithmeticException e){throw new RegraDeNegocioExcecao("Limite de estoque excedido na devolucao.");}
                estoques.save(new EstoqueEntidade(estoque.getId(),estoque.getUnidadeId(),estoque.getProdutoId(),quantidade));
            } else {
                perdas.save(new PerdaEstoqueEntidade(pedido.getId(),pedido.getUnidadeId(),item.getProdutoId(),item.getQuantidade()));
            }
        }
        pagamentos.estornarPedido(pedido);
        pedido.registrarCancelamento(motivo,repor);
        pedidos.save(pedido);
        auditoria.registrar(autor,"CANCELAR_PEDIDO","Pedido",pedido.getId(),
                motivo+"; estoque="+(repor?"REPOSTO":"PERDA_REGISTRADA"));
    }
    @Transactional
    public boolean cancelarExpirado(UUID id){
        var pedido=pedidos.buscarComBloqueio(id).orElse(null);
        if(pedido==null || pedido.getStatus()!=StatusPedido.AGUARDANDO_PAGAMENTO
                || pedido.getExpiraEm().isAfter(OffsetDateTime.now())) return false;
        cancelar(pedido,null,"PRAZO_PAGAMENTO_EXPIRADO");
        return true;
    }
}
