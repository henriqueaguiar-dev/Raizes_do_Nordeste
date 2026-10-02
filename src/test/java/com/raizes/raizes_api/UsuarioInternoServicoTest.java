package com.raizes.raizes_api;

import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioInternoRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.aplicacao.servico.UsuarioInternoServico;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UnidadeJpaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioInternoServicoTest {

    @Mock
    private UnidadeJpaRepositorio unidadeJpaRepositorio;

    @Mock
    private UsuarioServico usuarioServico;

    @InjectMocks
    private UsuarioInternoServico usuarioInternoServico;

    @Test
    void cadastrarInternoDelegatesCadastroQuandoUnidadeExiste() {
        UUID unidadeId = UUID.randomUUID();
        CadastrarUsuarioInternoRequisicao requisicao = requisicao(unidadeId);
        UsuarioResposta respostaEsperada = new UsuarioResposta(UUID.randomUUID(), "Atendente",
                "atendente@example.com", PerfilUsuario.ATENDENTE, true, true);
        when(unidadeJpaRepositorio.existsById(unidadeId)).thenReturn(true);
        when(usuarioServico.cadastrarInterno(requisicao)).thenReturn(respostaEsperada);

        UsuarioResposta resposta = usuarioInternoServico.cadastrarInterno(requisicao);

        assertEquals(respostaEsperada, resposta);
        verify(unidadeJpaRepositorio).existsById(unidadeId);
        verify(usuarioServico).cadastrarInterno(requisicao);
    }

    @Test
    void cadastrarInternoRejeitaUnidadeInexistente() {
        UUID unidadeId = UUID.randomUUID();
        CadastrarUsuarioInternoRequisicao requisicao = requisicao(unidadeId);
        when(unidadeJpaRepositorio.existsById(unidadeId)).thenReturn(false);

        assertThrows(RegraDeNegocioExcecao.class,
                () -> usuarioInternoServico.cadastrarInterno(requisicao));

        verify(usuarioServico, never()).cadastrarInterno(requisicao);
    }

    private CadastrarUsuarioInternoRequisicao requisicao(UUID unidadeId) {
        CadastrarUsuarioInternoRequisicao requisicao =
                org.mockito.Mockito.mock(CadastrarUsuarioInternoRequisicao.class);
        when(requisicao.getUnidadeId()).thenReturn(unidadeId);
        return requisicao;
    }
}