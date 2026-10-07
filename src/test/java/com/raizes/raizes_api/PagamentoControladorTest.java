package com.raizes.raizes_api;

import com.raizes.raizes_api.api.controlador.PagamentoControlador;
import com.raizes.raizes_api.aplicacao.servico.PagamentoServico;
import com.raizes.raizes_api.dominio.enums.MetodoPagamento;
import com.raizes.raizes_api.infraestrutura.seguranca.ConfiguracaoSeguranca;
import com.raizes.raizes_api.infraestrutura.seguranca.RespostaErroSeguranca;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagamentoControlador.class)
@Import({ConfiguracaoSeguranca.class, RespostaErroSeguranca.class})
class PagamentoControladorTest {
    @Autowired MockMvc mvc;
    @MockitoBean PagamentoServico servico;
    @MockitoBean JwtDecoder decoder;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"nao-e-uuid", "1-1-1-1-1", " 123e4567-e89b-12d3-a456-426614174000 "})
    void subjectInvalidoRetorna401NasDuasRotas(String subject) throws Exception {
        UUID pedidoId = UUID.randomUUID();
        var autenticacao = jwt().jwt(t -> {
            t.claims(c -> c.remove("sub"));
            if (subject != null) t.subject(subject);
            t.claim("perfil", "CLIENTE");
        });
        mvc.perform(get("/pagamentos/pedidos/{pedidoId}", pedidoId).with(autenticacao))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("CREDENCIAIS_INVALIDAS"));
        mvc.perform(post("/pagamentos/pedidos/{pedidoId}", pedidoId).with(autenticacao)
                        .contentType("application/json").content("{\"metodo\":\"MOCK\",\"aprovadoMock\":true}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("CREDENCIAIS_INVALIDAS"));
        verifyNoInteractions(servico);
    }

    @Test
    void subjectValidoIdentificaUsuarioNasDuasRotas() throws Exception {
        UUID pedidoId = UUID.randomUUID(), usuarioId = UUID.randomUUID();
        var autenticacao = jwt().jwt(t -> t.subject(usuarioId.toString()).claim("perfil", "CLIENTE"));
        mvc.perform(get("/pagamentos/pedidos/{pedidoId}", pedidoId).with(autenticacao))
                .andExpect(status().isOk());
        verify(servico).buscarPorPedido(pedidoId, usuarioId, "CLIENTE");
        mvc.perform(post("/pagamentos/pedidos/{pedidoId}", pedidoId).with(autenticacao)
                        .contentType("application/json").content("{\"metodo\":\"MOCK\",\"aprovadoMock\":true}"))
                .andExpect(status().isOk());
        verify(servico).processar(eq(pedidoId), argThat(r -> r.getMetodo() == MetodoPagamento.MOCK
                && Boolean.TRUE.equals(r.getAprovadoMock())), eq(usuarioId), eq("CLIENTE"));
    }
}
