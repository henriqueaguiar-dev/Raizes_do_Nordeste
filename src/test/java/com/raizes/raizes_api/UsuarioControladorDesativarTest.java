package com.raizes.raizes_api;

import com.raizes.raizes_api.api.controlador.UsuarioControlador;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.dominio.excecao.AcessoNegadoExcecao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsuarioControladorDesativarTest {

    @Mock
    private UsuarioServico usuarioServico;

    @InjectMocks
    private UsuarioControlador usuarioControlador;

    @Test
    void usuarioPodeDesativarProprioCadastro() {
        UUID usuarioId = UUID.randomUUID();

        usuarioControlador.desativar(usuarioId, jwt(usuarioId, "CLIENTE"));

        verify(usuarioServico).desativar(usuarioId);
    }

    @Test
    void administradorPodeDesativarOutroUsuario() {
        UUID administradorId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        usuarioControlador.desativar(usuarioId, jwt(administradorId, "ADMIN"));

        verify(usuarioServico).desativar(usuarioId);
    }

    @Test
    void usuarioNaoPodeDesativarCadastroDeOutraPessoa() {
        UUID solicitanteId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        assertThrows(AcessoNegadoExcecao.class,
                () -> usuarioControlador.desativar(usuarioId, jwt(solicitanteId, "CLIENTE")));

        verify(usuarioServico, never()).desativar(usuarioId);
    }

    private Jwt jwt(UUID usuarioId, String perfil) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(usuarioId.toString())
                .claim("perfil", perfil)
                .build();
    }
}