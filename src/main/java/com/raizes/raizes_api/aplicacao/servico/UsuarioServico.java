package com.raizes.raizes_api.aplicacao.servico;

import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioInternoRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioRequisicao;
import com.raizes.raizes_api.api.dto.requisicao.AtualizarUsuarioRequisicao;
import com.raizes.raizes_api.api.dto.resposta.UsuarioResposta;
import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UsuarioJpaRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UsuarioServico {

    private final UsuarioJpaRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServico(UsuarioJpaRepositorio usuarioRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResposta cadastrar(CadastrarUsuarioRequisicao requisicao) {
        if (usuarioRepositorio.existsByEmail(requisicao.getEmail())) {
            throw new RegraDeNegocioExcecao("Email ja cadastrado.");
        }

        String senhaHash = passwordEncoder.encode(requisicao.getSenha());

        UsuarioEntidade usuario = new UsuarioEntidade(
                UUID.randomUUID(),
                requisicao.getNome(),
                requisicao.getEmail(),
                senhaHash,
                PerfilUsuario.CLIENTE,
                true,
                requisicao.getConsentimentoLgpd(),
                OffsetDateTime.now());

        UsuarioEntidade usuarioSalvo = usuarioRepositorio.save(usuario);

        return paraResposta(usuarioSalvo);
    }

    public UsuarioResposta cadastrarInterno(CadastrarUsuarioInternoRequisicao requisicao) {
        if (usuarioRepositorio.existsByEmail(requisicao.getEmail())) {
            throw new RegraDeNegocioExcecao("Email ja cadastrado.");
        }

        if (requisicao.getPerfil() == PerfilUsuario.CLIENTE) {
            throw new RegraDeNegocioExcecao("Use o cadastro publico para criar usuarios clientes.");
        }

        String senhaHash = passwordEncoder.encode(requisicao.getSenha());

        UsuarioEntidade usuario = new UsuarioEntidade(
                UUID.randomUUID(),
                requisicao.getNome(),
                requisicao.getEmail(),
                senhaHash,
                requisicao.getPerfil(),
                true,
                requisicao.getConsentimentoLgpd(),
                OffsetDateTime.now());

        UsuarioEntidade usuarioSalvo = usuarioRepositorio.save(usuario);

        return paraResposta(usuarioSalvo);
    }
    

    @Transactional
    public UsuarioResposta atualizar(UUID id, AtualizarUsuarioRequisicao requisicao) {
        UsuarioEntidade usuarioAtual = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoExcecao("Usuario nao encontrado."));

        if (!usuarioAtual.getEmail().equals(requisicao.getEmail())
                && usuarioRepositorio.existsByEmail(requisicao.getEmail())) {
            throw new RegraDeNegocioExcecao("Email ja cadastrado.");
        }

        UsuarioEntidade usuarioAtualizado = new UsuarioEntidade(
                usuarioAtual.getId(),
                requisicao.getNome(),
                requisicao.getEmail(),
                usuarioAtual.getSenhaHash(),
                usuarioAtual.getPerfil(),
                usuarioAtual.isAtivo(),
                requisicao.getConsentimentoLgpd(),
                usuarioAtual.getCriadoEm());

        return paraResposta(usuarioRepositorio.save(usuarioAtualizado));
    }

    private UsuarioResposta paraResposta(UsuarioEntidade usuario) {
        return new UsuarioResposta(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.isConsentimentoLgpd());
    }
    
}
