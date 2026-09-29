package com.raizes.raizes_api.infraestrutura.configuracao;

import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UsuarioJpaRepositorio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
public class SeedAdminInicial implements CommandLineRunner {

    private final UsuarioJpaRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin-inicial.nome}")
    private String nome;

    @Value("${app.admin-inicial.email}")
    private String email;

    @Value("${app.admin-inicial.senha}")
    private String senha;

    public SeedAdminInicial(UsuarioJpaRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        System.out.println("Executando seed do admin inicial: " + email);

        if (usuarioRepositorio.existsByEmail(email)) {
            return;
        }

        UsuarioEntidade admin = new UsuarioEntidade(
                UUID.randomUUID(),
                nome,
                email,
                passwordEncoder.encode(senha),
                PerfilUsuario.ADMIN,
                true,
                true,
                OffsetDateTime.now()
        );

        usuarioRepositorio.save(admin);
    }
}
