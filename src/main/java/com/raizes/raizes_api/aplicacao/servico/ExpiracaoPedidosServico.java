package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.dominio.enums.StatusPedido;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.PedidoJpaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.OffsetDateTime;

@Service
public class ExpiracaoPedidosServico {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(ExpiracaoPedidosServico.class);
    private final PedidoJpaRepositorio pedidos;
    private final CancelamentoPedidoServico cancelamento;
    private final boolean habilitada;
    public ExpiracaoPedidosServico(PedidoJpaRepositorio pedidos,CancelamentoPedidoServico cancelamento,
            @Value("${app.pedidos.expiracao.habilitada:true}") boolean habilitada){
        this.pedidos=pedidos;this.cancelamento=cancelamento;this.habilitada=habilitada;
    }
    @Scheduled(fixedDelayString="${app.pedidos.expiracao.intervalo-ms:60000}")
    public void executarAgendado(){if(habilitada) expirar();}
    public int expirar(){
        int quantidade=0;
        for(var pedido:pedidos.findTop100ByStatusAndExpiraEmLessThanEqualOrderByExpiraEmAsc(
                StatusPedido.AGUARDANDO_PAGAMENTO,OffsetDateTime.now())) {
            try {if(cancelamento.cancelarExpirado(pedido.getId())) quantidade++;}
            catch(RuntimeException e){LOG.error("Falha ao expirar pedido {}",pedido.getId(),e);}
        }
        return quantidade;
    }
}
