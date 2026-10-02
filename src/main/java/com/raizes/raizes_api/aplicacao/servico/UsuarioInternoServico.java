package com.raizes.raizes_api.aplicacao.servico;

import org.springframework.stereotype.Service;

import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioInternoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UnidadeJpaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class UsuarioInternoServico {

    private final UnidadeJpaRepositorio unidadeJpaRepositorio;

    private final UsuarioServico usuarioServico;

    public UsuarioResposta cadastrarInterno(CadastrarUsuarioInternoRequisicao requisicao) {
        boolean existeUnidade = unidadeJpaRepositorio.existsById(requisicao.getUnidadeId());
        if (!existeUnidade) {
            throw new RegraDeNegocioExcecao("Unidade não encontrada.");
        }
        return usuarioServico.cadastrarInterno(requisicao);
    }
}
