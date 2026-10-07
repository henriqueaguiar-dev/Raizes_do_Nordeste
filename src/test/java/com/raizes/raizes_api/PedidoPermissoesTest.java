package com.raizes.raizes_api;

import com.raizes.raizes_api.api.controlador.PedidoControlador;
import com.raizes.raizes_api.aplicacao.servico.PedidoServico;
import com.raizes.raizes_api.dominio.enums.StatusPedido;
import com.raizes.raizes_api.infraestrutura.seguranca.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoControlador.class)
@Import({ConfiguracaoSeguranca.class, RespostaErroSeguranca.class})
class PedidoPermissoesTest {
    @Autowired MockMvc mvc;
    @MockitoBean PedidoServico servico;
    @MockitoBean JwtDecoder decoder;
    @ParameterizedTest @ValueSource(strings={"ADMIN","GERENTE","COZINHA","ATENDENTE"})
    void operadorPodeAlterarStatusEIdentificaAutor(String perfil) throws Exception {
        UUID pedido=UUID.randomUUID(),autor=UUID.randomUUID();
        mvc.perform(patch("/pedidos/{id}/status",pedido).contentType("application/json").content("{\"status\":\"EM_PREPARO\"}")
                .with(jwt().jwt(t -> t.subject(autor.toString()).claim("perfil",perfil))
                        .authorities(new SimpleGrantedAuthority("ROLE_"+perfil))))
                .andExpect(status().isOk());
        verify(servico).atualizarStatus(pedido,StatusPedido.EM_PREPARO,autor,perfil);
    }
    @ParameterizedTest @ValueSource(strings={"CLIENTE","DESCONHECIDO"})
    void clienteNaoPodeAlterarStatus(String perfil) throws Exception {
        mvc.perform(patch("/pedidos/{id}/status",UUID.randomUUID()).contentType("application/json").content("{\"status\":\"CANCELADO\"}")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_"+perfil))))
                .andExpect(status().isForbidden());
        verifyNoInteractions(servico);
    }
}
