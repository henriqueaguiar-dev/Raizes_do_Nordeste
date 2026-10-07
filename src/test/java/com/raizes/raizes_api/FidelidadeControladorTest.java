package com.raizes.raizes_api;

import com.raizes.raizes_api.api.controlador.FidelidadeControlador;
import com.raizes.raizes_api.api.dto.resposta.FidelidadeSaldoResposta;
import com.raizes.raizes_api.aplicacao.servico.FidelidadeServico;
import com.raizes.raizes_api.infraestrutura.seguranca.ConfiguracaoSeguranca;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.h2console.autoconfigure.H2ConsoleProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FidelidadeControlador.class)
@Import({ConfiguracaoSeguranca.class, com.raizes.raizes_api.infraestrutura.seguranca.RespostaErroSeguranca.class})
@EnableConfigurationProperties(H2ConsoleProperties.class)
class FidelidadeControladorTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private FidelidadeServico fidelidadeServico;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    void consultaClienteDoTokenMesmoSeInformadoOutroId() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(fidelidadeServico.consultarSaldo(clienteId))
                .thenReturn(new FidelidadeSaldoResposta(clienteId, 20));
        mockMvc.perform(get("/fidelidade/saldo")
                        .param("clienteId", UUID.randomUUID().toString())
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE")).jwt(token -> token.subject(clienteId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.pontos").value(20));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GERENTE", "ATENDENTE", "COZINHA"})
    void perfilInternoPodeConsultarProprioSaldo(String perfil) throws Exception {
        UUID id = UUID.randomUUID();
        when(fidelidadeServico.consultarSaldo(id)).thenReturn(new FidelidadeSaldoResposta(id, 5));
        mockMvc.perform(get("/fidelidade/saldo/{clienteId}", id)
                .with(jwt().jwt(t -> t.subject(id.toString()).claim("perfil", perfil))
                        .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + perfil))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pontos").value(5));
    }

    @Test
    void exigeAutenticacao() throws Exception {
        mockMvc.perform(get("/fidelidade/saldo")).andExpect(status().isUnauthorized());
        verifyNoInteractions(fidelidadeServico);
    }

    @Test
    void titularPodeConsultarSeuSaldoPorId() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(fidelidadeServico.consultarSaldo(clienteId))
                .thenReturn(new FidelidadeSaldoResposta(clienteId, 15));
        mockMvc.perform(get("/fidelidade/saldo/{clienteId}", clienteId)
                        .with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CLIENTE")).jwt(token -> token.subject(clienteId.toString()).claim("perfil", "CLIENTE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontos").value(15));
    }

    @Test
    void adminPodeConsultarSaldoDeOutroUsuario() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(fidelidadeServico.consultarSaldo(clienteId))
                .thenReturn(new FidelidadeSaldoResposta(clienteId, 30));
        mockMvc.perform(get("/fidelidade/saldo/{clienteId}", clienteId)
                        .with(jwt().jwt(token -> token.subject(UUID.randomUUID().toString()).claim("perfil", "ADMIN"))
                                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.pontos").value(30));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CLIENTE", "GERENTE", "ATENDENTE", "COZINHA"})
    void outrosPerfisNaoPodemConsultarOutraConta(String perfil) throws Exception {
        mockMvc.perform(get("/fidelidade/saldo/{clienteId}", UUID.randomUUID())
                        .with(jwt().jwt(token -> token.subject(UUID.randomUUID().toString()).claim("perfil", perfil))
                                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + perfil))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(fidelidadeServico);
    }

    @Test
    void consultaPorIdExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/fidelidade/saldo/{clienteId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(fidelidadeServico);
    }
}
