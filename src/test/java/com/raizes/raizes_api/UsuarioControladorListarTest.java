package com.raizes.raizes_api;

import com.raizes.raizes_api.api.controlador.UsuarioControlador;
import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.aplicacao.servico.UsuarioInternoServico;
import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.infraestrutura.seguranca.ConfiguracaoSeguranca;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioControlador.class)
@Import({ConfiguracaoSeguranca.class, com.raizes.raizes_api.infraestrutura.seguranca.RespostaErroSeguranca.class})
class UsuarioControladorListarTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioServico usuarioServico;

    @MockitoBean
    private UsuarioInternoServico usuarioInternoServico;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void administradorPodeListarUsuarios() throws Exception {
        when(usuarioServico.listarCliente()).thenReturn(List.of(new UsuarioResposta(UUID.randomUUID(), "Admin",
                "admin@example.com", PerfilUsuario.ADMIN, true, true)));

        mockMvc.perform(get("/usuarios").with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@example.com"));

        verify(usuarioServico).listarCliente();
    }

    @Test
    void clienteAutenticadoNaoPodeListarUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios").with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")))))
                .andExpect(status().isForbidden());

        verify(usuarioServico, never()).listarCliente();
    }

    @Test
    void administradorPodeListarColaboradores() throws Exception {
        when(usuarioServico.listarColaboradores()).thenReturn(List.of(new UsuarioResposta(UUID.randomUUID(),
                "Atendente", "atendente@example.com", PerfilUsuario.ATENDENTE, true, true)));

        mockMvc.perform(get("/usuarios/internos")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("atendente@example.com"));

        verify(usuarioServico).listarColaboradores();
    }

    @Test
    void clienteAutenticadoNaoPodeListarColaboradores() throws Exception {
        mockMvc.perform(get("/usuarios/internos")
                        .with(jwt().authorities(List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")))))
                .andExpect(status().isForbidden());

        verify(usuarioServico, never()).listarColaboradores();
    }
}