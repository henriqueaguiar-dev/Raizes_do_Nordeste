package com.raizes.raizes_api;

import com.raizes.raizes_api.api.dto.requisicao.AtualizarUsuarioRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UsuarioJpaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServicoAtualizarTest {

    @Mock
    private UsuarioJpaRepositorio usuarioRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServico usuarioServico;

    @Test
    void atualizarAlteraDadosPermitidosEPreservaCamposProtegidos() {
        UUID usuarioId = UUID.randomUUID();
        OffsetDateTime criadoEm = OffsetDateTime.parse("2025-01-01T10:00:00Z");
        UsuarioEntidade usuarioAtual = new UsuarioEntidade(usuarioId, "Nome antigo", "antigo@example.com",
                "senha-hash", PerfilUsuario.GERENTE, false, false, criadoEm);
        AtualizarUsuarioRequisicao requisicao = requisicao("Nome novo", "novo@example.com", true);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(usuarioAtual));
        when(usuarioRepositorio.existsByEmail("novo@example.com")).thenReturn(false);
        when(usuarioRepositorio.save(any(UsuarioEntidade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioResposta resposta = usuarioServico.atualizar(usuarioId, requisicao);

        ArgumentCaptor<UsuarioEntidade> usuarioCapturado = ArgumentCaptor.forClass(UsuarioEntidade.class);
        verify(usuarioRepositorio).save(usuarioCapturado.capture());
        UsuarioEntidade salvo = usuarioCapturado.getValue();
        assertEquals(usuarioId, salvo.getId());
        assertEquals("Nome novo", resposta.getNome());
        assertEquals("novo@example.com", resposta.getEmail());
        assertTrue(resposta.isConsentimentoLgpd());
        assertEquals("senha-hash", salvo.getSenhaHash());
        assertEquals(PerfilUsuario.GERENTE, salvo.getPerfil());
        assertFalse(salvo.isAtivo());
        assertEquals(criadoEm, salvo.getCriadoEm());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void atualizarPermiteManterOProprioEmail() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId, "ana@example.com")));
        when(usuarioRepositorio.save(any(UsuarioEntidade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usuarioServico.atualizar(usuarioId, requisicao("Ana", "ana@example.com", true));

        verify(usuarioRepositorio, never()).existsByEmail(anyString());
    }

    @Test
    void atualizarRejeitaEmailDeOutroUsuario() {
        UUID usuarioId = UUID.randomUUID();
        AtualizarUsuarioRequisicao requisicao = org.mockito.Mockito.mock(AtualizarUsuarioRequisicao.class);
        when(requisicao.getEmail()).thenReturn("outro@example.com");
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId, "ana@example.com")));
        when(usuarioRepositorio.existsByEmail("outro@example.com")).thenReturn(true);

        assertThrows(RegraDeNegocioExcecao.class, () -> usuarioServico.atualizar(usuarioId, requisicao));

        verify(usuarioRepositorio, never()).save(any(UsuarioEntidade.class));
    }

    @Test
    void atualizarUsuarioInexistenteLancaExcecao() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoExcecao.class,
                () -> usuarioServico.atualizar(usuarioId, org.mockito.Mockito.mock(AtualizarUsuarioRequisicao.class)));

        verify(usuarioRepositorio, never()).save(any(UsuarioEntidade.class));
    }

    private AtualizarUsuarioRequisicao requisicao(String nome, String email, boolean consentimento) {
        AtualizarUsuarioRequisicao requisicao = org.mockito.Mockito.mock(AtualizarUsuarioRequisicao.class);
        when(requisicao.getNome()).thenReturn(nome);
        when(requisicao.getEmail()).thenReturn(email);
        when(requisicao.getConsentimentoLgpd()).thenReturn(consentimento);
        return requisicao;
    }

    private UsuarioEntidade usuario(UUID id, String email) {
        return new UsuarioEntidade(id, "Ana", email, "senha-hash", PerfilUsuario.CLIENTE,
                true, false, OffsetDateTime.parse("2025-01-01T10:00:00Z"));
    }
}