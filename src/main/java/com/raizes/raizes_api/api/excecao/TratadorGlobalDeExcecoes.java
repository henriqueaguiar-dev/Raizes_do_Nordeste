package com.raizes.raizes_api.api.excecao;

import com.raizes.raizes_api.api.dto.resposta.DetalheErroResposta;
import com.raizes.raizes_api.api.dto.resposta.ErroResposta;
import com.raizes.raizes_api.dominio.excecao.AcessoNegadoExcecao;
import com.raizes.raizes_api.dominio.excecao.CredenciaisInvalidasExcecao;
import com.raizes.raizes_api.dominio.excecao.EstoqueInsuficienteExcecao;
import com.raizes.raizes_api.dominio.excecao.RecursoNaoEncontradoExcecao;
import com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao;
import com.raizes.raizes_api.dominio.excecao.StatusPedidoInvalidoExcecao;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class TratadorGlobalDeExcecoes {

    @ExceptionHandler(RecursoNaoEncontradoExcecao.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoExcecao excecao,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.NOT_FOUND, "RECURSO_NAO_ENCONTRADO", excecao.getMessage(), List.of(), request);
    }

    @ExceptionHandler(EstoqueInsuficienteExcecao.class)
    public ResponseEntity<ErroResposta> tratarEstoqueInsuficiente(
            EstoqueInsuficienteExcecao excecao,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.CONFLICT, "ESTOQUE_INSUFICIENTE", excecao.getMessage(), List.of(), request);
    }

    @ExceptionHandler(StatusPedidoInvalidoExcecao.class)
    public ResponseEntity<ErroResposta> tratarStatusPedidoInvalido(
            StatusPedidoInvalidoExcecao excecao,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.CONFLICT, "STATUS_PEDIDO_INVALIDO", excecao.getMessage(), List.of(), request);
    }

    @ExceptionHandler(RegraDeNegocioExcecao.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(
            RegraDeNegocioExcecao excecao,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.CONFLICT, "REGRA_DE_NEGOCIO", excecao.getMessage(), List.of(), request);
    }

    @ExceptionHandler(CredenciaisInvalidasExcecao.class)
    public ResponseEntity<ErroResposta> tratarCredenciaisInvalidas(
            CredenciaisInvalidasExcecao excecao,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", excecao.getMessage(), List.of(), request);
    }

    @ExceptionHandler(AcessoNegadoExcecao.class)
    public ResponseEntity<ErroResposta> tratarAcessoNegado(
            AcessoNegadoExcecao excecao,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", excecao.getMessage(), List.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(
            MethodArgumentNotValidException excecao,
            HttpServletRequest request
    ) {
        List<DetalheErroResposta> detalhes = excecao.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> new DetalheErroResposta(erro.getField(), erro.getDefaultMessage()))
                .toList();

        return criarResposta(HttpStatus.UNPROCESSABLE_ENTITY, "DADOS_INVALIDOS",
                "Existem campos invalidos na requisicao.", detalhes, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInterno(
            Exception excecao,
            HttpServletRequest request
    ) {
        return criarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Ocorreu um erro interno inesperado.", List.of(), request);
    }

    private ResponseEntity<ErroResposta> criarResposta(
            HttpStatus status,
            String erro,
            String mensagem,
            List<DetalheErroResposta> detalhes,
            HttpServletRequest request
    ) {
        ErroResposta resposta = new ErroResposta(
                erro,
                mensagem,
                detalhes,
                OffsetDateTime.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(resposta);
    }
}
