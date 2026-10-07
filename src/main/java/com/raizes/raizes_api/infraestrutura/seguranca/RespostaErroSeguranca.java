package com.raizes.raizes_api.infraestrutura.seguranca;

import com.raizes.raizes_api.api.dto.resposta.ErroResposta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class RespostaErroSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper mapper;
    public RespostaErroSeguranca(ObjectMapper mapper) { this.mapper = mapper; }
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        responder(req, res, 401, "NAO_AUTENTICADO", "Autenticacao necessaria ou token invalido.");
    }
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        responder(req, res, 403, "ACESSO_NEGADO", "Voce nao tem permissao para acessar este recurso.");
    }
    private void responder(HttpServletRequest req, HttpServletResponse res, int status, String codigo, String mensagem) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(mapper.writeValueAsString(new ErroResposta(codigo, mensagem, List.of(), OffsetDateTime.now(), req.getRequestURI())));
    }
}
