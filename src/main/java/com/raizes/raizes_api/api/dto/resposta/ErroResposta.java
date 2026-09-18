package com.raizes.raizes_api.api.dto.resposta;

import java.time.OffsetDateTime;
import java.util.List;

public class ErroResposta {

    private String erro;
    private String mensagem;
    private List<DetalheErroResposta> detalhes;
    private OffsetDateTime dataHora;
    private String caminho;

    public ErroResposta(String erro, String mensagem, List<DetalheErroResposta> detalhes,
                        OffsetDateTime dataHora, String caminho) {
        this.erro = erro;
        this.mensagem = mensagem;
        this.detalhes = detalhes;
        this.dataHora = dataHora;
        this.caminho = caminho;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public List<DetalheErroResposta> getDetalhes() {
        return detalhes;
    }

    public OffsetDateTime getDataHora() {
        return dataHora;
    }

    public String getCaminho() {
        return caminho;
    }
}
