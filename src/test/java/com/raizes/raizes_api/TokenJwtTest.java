package com.raizes.raizes_api;

import com.raizes.raizes_api.infraestrutura.seguranca.*;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;
import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class TokenJwtTest {
    @Test void tokenUsaExpiracaoConfiguradaEConvertePerfil() {
        ConfiguracaoJwt config=new ConfiguracaoJwt();
        ReflectionTestUtils.setField(config,"segredoJwt","segredo-teste-com-pelo-menos-trinta-e-dois-bytes");
        ServicoTokenJwt servico=new ServicoTokenJwt(config.jwtEncoder());
        ReflectionTestUtils.setField(servico,"expiracaoEmMinutos",30L);
        UUID id=UUID.randomUUID();
        var usuario=new UsuarioEntidade(id,"Admin","a@teste.local","hash",PerfilUsuario.ADMIN,true,true,OffsetDateTime.now());
        var jwt=config.jwtDecoder().decode(servico.gerarToken(usuario));
        assertEquals(1800,servico.obterExpiracaoEmSegundos());
        assertEquals(1800,jwt.getExpiresAt().getEpochSecond()-jwt.getIssuedAt().getEpochSecond());
        assertEquals(id.toString(),jwt.getSubject());
        assertTrue(new ConversorJwtAutoridades().convert(jwt).getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
