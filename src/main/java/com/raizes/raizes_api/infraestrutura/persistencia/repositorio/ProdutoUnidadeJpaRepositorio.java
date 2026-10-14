package com.raizes.raizes_api.infraestrutura.persistencia.repositorio;

import com.raizes.raizes_api.api.dto.resposta.CardapioItemResposta;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.ProdutoUnidadeEntidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoUnidadeJpaRepositorio extends JpaRepository<ProdutoUnidadeEntidade, UUID> {
    Optional<ProdutoUnidadeEntidade> findByUnidadeIdAndProdutoId(UUID unidadeId, UUID produtoId);
    boolean existsByUnidadeIdAndProdutoIdAndAtivoTrue(UUID unidadeId, UUID produtoId);

    @Query("""
            select new com.raizes.raizes_api.api.dto.resposta.CardapioItemResposta(
                p.id, p.nome, p.descricao, p.preco,
                case when e.quantidadeDisponivel > 0 then true else false end)
            from ProdutoUnidadeEntidade vinculo
            join ProdutoEntidade p on p.id = vinculo.produtoId
            left join EstoqueEntidade e on e.produtoId = p.id and e.unidadeId = vinculo.unidadeId
            where vinculo.unidadeId = :unidadeId and vinculo.ativo = true and p.ativo = true
            order by p.nome, p.id
            """)
    List<CardapioItemResposta> consultarCardapio(@Param("unidadeId") UUID unidadeId);
}
