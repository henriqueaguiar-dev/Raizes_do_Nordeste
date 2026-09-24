package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.LoginRequisicao;
import com.raizes.raizes_api.api.dto.resposta.LoginResposta;
import com.raizes.raizes_api.dominio.excecao.CredenciaisInvalidasExcecao;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UsuarioJpaRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoServico {

    private final UsuarioJpaRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoServico(UsuarioJpaRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResposta login(LoginRequisicao requisicao) {
        UsuarioEntidade usuario = usuarioRepositorio.findByEmail(requisicao.getEmail())
                .orElseThrow(() -> new CredenciaisInvalidasExcecao("Email ou senha invalidos."));

        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioExcecao("Usuario inativo.");
        }

        boolean senhaCorreta = passwordEncoder.matches(requisicao.getSenha(), usuario.getSenhaHash());

        if (!senhaCorreta) {
            throw new CredenciaisInvalidasExcecao("Email ou senha invalidos.");
        }

        return new LoginResposta(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                "Login realizado com sucesso."
        );
    }
}
