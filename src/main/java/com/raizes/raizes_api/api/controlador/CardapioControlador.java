package com.raizes.raizes_api.api.controlador;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarCardapioRequisicao;
import com.raizes.raizes_api.api.dto.resposta.CardapioItemResposta;
import com.raizes.raizes_api.api.dto.resposta.CardapioVinculoResposta;
import com.raizes.raizes_api.aplicacao.servico.CardapioServico;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/unidades/{unidadeId}/cardapio")
@Tag(name = "04 - Cardapio", description = "Consulta e configuracao do cardapio por unidade")
public class CardapioControlador {
    private final CardapioServico cardapioServico;

    public CardapioControlador(CardapioServico cardapioServico) {
        this.cardapioServico = cardapioServico;
    }

    @GetMapping
    @SecurityRequirements
    public List<CardapioItemResposta> consultar(@PathVariable UUID unidadeId) {
        return cardapioServico.consultar(unidadeId);
    }

    @PutMapping("/produtos/{produtoId}")
    public CardapioVinculoResposta atualizar(@PathVariable UUID unidadeId, @PathVariable UUID produtoId,
                                            @Valid @RequestBody AtualizarCardapioRequisicao requisicao) {
        return cardapioServico.atualizar(unidadeId, produtoId, requisicao);
    }
}
