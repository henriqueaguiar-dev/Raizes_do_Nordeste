package com.raizes.raizes_api;

import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UsuarioJpaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServicoListarTest {

    @Mock
    private UsuarioJpaRepositorio usuarioRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServico usuarioServico;

    @Test
    void listarRetornaUsuariosAtivosEInativosComoRespostasPublicas() {
        when(usuarioRepositorio.findByPerfil(PerfilUsuario.CLIENTE)).thenReturn(List.of(usuario("ativo@example.com", true),
                usuario("inativo@example.com", false)));

        List<UsuarioResposta> resposta = usuarioServico.listarCliente();

        assertEquals(2, resposta.size());
        assertEquals("ativo@example.com", resposta.get(0).getEmail());
        assertTrue(resposta.get(0).isAtivo());
        assertEquals("inativo@example.com", resposta.get(1).getEmail());
        assertFalse(resposta.get(1).isAtivo());
        verify(usuarioRepositorio).findByPerfil(PerfilUsuario.CLIENTE);
    }

    private UsuarioEntidade usuario(String email, boolean ativo) {
        return new UsuarioEntidade(UUID.randomUUID(), "Usuario", email, "hash-nao-exposto",
                PerfilUsuario.CLIENTE, ativo, true, OffsetDateTime.now());
    }
}