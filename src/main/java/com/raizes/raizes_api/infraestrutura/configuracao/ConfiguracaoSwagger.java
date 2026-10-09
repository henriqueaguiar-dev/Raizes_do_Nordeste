package com.raizes.raizes_api.infraestrutura.configuracao;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoSwagger {

    @Bean
    public OpenAPI openAPI() {
        String esquemaSeguranca = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Raizes API")
                        .description("API REST para gerenciamento de unidades,\n" + //
                                                                "            usuários, produtos, estoque, pedidos,\n" + //
                                                                "            pagamentos e programa de fidelidade.\n" + //
                                                                "\n" + //
                                                                "            Desenvolvida com Java 21 e Spring Boot.\n" + //
                                                                "\n" + //
                                                                "            Autenticação baseada em JWT e controle\n" + //
                                                                "            de acesso por perfis de usuário.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Henrique da Silva Aguiar").url("https://github.com/henriqueaguiar-dev")))
                .addSecurityItem(new SecurityRequirement().addList(esquemaSeguranca))
                .components(new Components()
                        .addSecuritySchemes(esquemaSeguranca, new SecurityScheme()
                                .name(esquemaSeguranca)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
