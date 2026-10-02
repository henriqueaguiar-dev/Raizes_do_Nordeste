package com.raizes.raizes_api;

import com.raizes.raizes_api.api.dto.requisicao.CadastrarUsuarioInternoRequisicao;
import com.raizes.raizes_api.aplicacao.servico.UsuarioServico;
import com.raizes.raizes_api.dominio.enums.PerfilUsuario;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.UsuarioEntidade;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.UsuarioJpaRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServicoCadastrarInternoTest {

    @Mock
    private UsuarioJpaRepositorio usuarioRepositorio;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServico usuarioServico;

    @Test
    void cadastrarInternoPersisteUuidDaUnidadeDaRequisicao() {
        UUID unidadeId = UUID.randomUUID();
        CadastrarUsuarioInternoRequisicao requisicao =
                org.mockito.Mockito.mock(CadastrarUsuarioInternoRequisicao.class);
        when(requisicao.getNome()).thenReturn("Atendente");
        when(requisicao.getEmail()).thenReturn("atendente@example.com");
        when(requisicao.getSenha()).thenReturn("senha-segura");
        when(requisicao.getPerfil()).thenReturn(PerfilUsuario.ATENDENTE);
        when(requisicao.getConsentimentoLgpd()).thenReturn(true);
        when(requisicao.getUnidadeId()).thenReturn(unidadeId);
        when(usuarioRepositorio.existsByEmail("atendente@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha-segura")).thenReturn("senha-hash");
        when(usuarioRepositorio.save(any(UsuarioEntidade.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        usuarioServico.cadastrarInterno(requisicao);

        ArgumentCaptor<UsuarioEntidade> entidadeCapturada = ArgumentCaptor.forClass(UsuarioEntidade.class);
        org.mockito.Mockito.verify(usuarioRepositorio).save(entidadeCapturada.capture());
        assertEquals(unidadeId, entidadeCapturada.getValue().getUnidadeId());
        assertEquals(OffsetDateTime.class, entidadeCapturada.getValue().getCriadoEm().getClass());
    }
}