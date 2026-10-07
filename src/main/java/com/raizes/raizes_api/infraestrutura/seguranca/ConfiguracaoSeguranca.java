package com.raizes.raizes_api.infraestrutura.seguranca;

import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfiguracaoSeguranca {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, RespostaErroSeguranca erros) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/h2-console/**").permitAll()
                                                .requestMatchers("/auth/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/usuarios/internos").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/usuarios/internos").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                                                .requestMatchers(HttpMethod.PUT, "/usuarios/internos/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/usuarios/internos/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/usuarios/**").hasAnyRole("CLIENTE", "ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/usuarios/**").hasAnyRole("CLIENTE", "ADMIN")

                                                .requestMatchers("/fidelidade/saldo", "/fidelidade/saldo/**").authenticated()

                                                .requestMatchers(HttpMethod.GET, "/produtos/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/unidades/**").permitAll()

                                                .requestMatchers(HttpMethod.POST, "/produtos/**")
                                                .hasAnyRole("ADMIN", "GERENTE")
                                                .requestMatchers(HttpMethod.PUT, "/produtos/**")
                                                .hasAnyRole("ADMIN", "GERENTE")
                                                .requestMatchers(HttpMethod.DELETE, "/produtos/**")
                                                .hasAnyRole("ADMIN", "GERENTE")

                                                .requestMatchers(HttpMethod.POST, "/unidades/**")
                                                .hasAnyRole("ADMIN", "GERENTE")
                                                .requestMatchers(HttpMethod.PUT, "/unidades/**")
                                                .hasAnyRole("ADMIN", "GERENTE")
                                                .requestMatchers(HttpMethod.DELETE, "/unidades/**")
                                                .hasAnyRole("ADMIN", "GERENTE")

                                                .requestMatchers("/estoques/**").hasAnyRole("ADMIN", "GERENTE")

                                                .requestMatchers(HttpMethod.PATCH, "/pedidos/*/status")
                                                .hasAnyRole("ADMIN", "GERENTE", "COZINHA", "ATENDENTE")
                                                
                                                .requestMatchers("/pedidos/**").authenticated()
                                                .requestMatchers("/pagamentos/**").authenticated()

                                                .requestMatchers(PathRequest.toH2Console()).permitAll()
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .requestMatchers("/auth/**").permitAll()

                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(erros).accessDeniedHandler(erros))
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .authenticationEntryPoint(erros).accessDeniedHandler(erros)
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                                new ConversorJwtAutoridades())))
                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()))
                                .build();
        }
}