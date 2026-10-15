package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.PerdaEstoqueEntidade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PerdaEstoqueJpaRepositorio extends JpaRepository<PerdaEstoqueEntidade,UUID> {
    List<PerdaEstoqueEntidade> findByPedidoId(UUID pedidoId);
}
