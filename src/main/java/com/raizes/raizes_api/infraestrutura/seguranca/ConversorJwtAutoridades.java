package com.raizes.raizes_api.infraestrutura.seguranca;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

public class ConversorJwtAutoridades implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String perfil = jwt.getClaimAsString("perfil");

        List<SimpleGrantedAuthority> autoridades = List.of(
                new SimpleGrantedAuthority("ROLE_" + perfil)
        );

        return new JwtAuthenticationToken(jwt, autoridades, jwt.getSubject());
    }
}
