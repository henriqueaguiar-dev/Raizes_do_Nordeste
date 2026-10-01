package com.raizes.raizes_api;

import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServicoDesativarTest {

    @Mock
    private UsuarioJpaRepositorio usuarioRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServico usuarioServico;

    @Test
    void desativarMantemRegistroEDadosMasMarcaUsuarioInativo() {
        UUID usuarioId = UUID.randomUUID();
        OffsetDateTime criadoEm = OffsetDateTime.parse("2025-02-03T12:00:00Z");
        UsuarioEntidade usuario = new UsuarioEntidade(usuarioId, "Ana", "ana@example.com", "senha-hash",
                PerfilUsuario.GERENTE, true, true, criadoEm);
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepositorio.save(any(UsuarioEntidade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usuarioServico.desativar(usuarioId);

        ArgumentCaptor<UsuarioEntidade> usuarioCapturado = ArgumentCaptor.forClass(UsuarioEntidade.class);
        verify(usuarioRepositorio).save(usuarioCapturado.capture());
        UsuarioEntidade salvo = usuarioCapturado.getValue();
        assertEquals(usuarioId, salvo.getId());
        assertEquals("Ana", salvo.getNome());
        assertEquals("ana@example.com", salvo.getEmail());
        assertEquals("senha-hash", salvo.getSenhaHash());
        assertEquals(PerfilUsuario.GERENTE, salvo.getPerfil());
        assertFalse(salvo.isAtivo());
        assertEquals(true, salvo.isConsentimentoLgpd());
        assertEquals(criadoEm, salvo.getCriadoEm());
    }

    @Test
    void desativarUsuarioInexistenteLancaExcecaoSemSalvar() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepositorio.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoExcecao.class, () -> usuarioServico.desativar(usuarioId));

        verify(usuarioRepositorio, never()).save(any(UsuarioEntidade.class));
    }
}