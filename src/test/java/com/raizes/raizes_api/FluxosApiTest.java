package com.raizes.raizes_api;

import com.raizes.raizes_api.aplicacao.servico.PedidoServico;
import com.raizes.raizes_api.api.dto.requisicao.CriarPedidoRequisicao;
import com.raizes.raizes_api.infraestrutura.persistencia.repositorio.*;
import com.raizes.raizes_api.infraestrutura.persistencia.entidade.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "app.security.jwt.secret=teste-segredo-com-pelo-menos-trinta-e-dois-bytes",
    "app.admin-inicial.nome=Administrador Teste",
    "app.admin-inicial.email=admin@teste.local",
    "app.admin-inicial.senha=SenhaTeste123"
})
@AutoConfigureMockMvc
class FluxosApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired EstoqueJpaRepositorio estoques;
    @Autowired PedidoJpaRepositorio pedidos;
    @Autowired PagamentoJpaRepositorio pagamentos;
    @Autowired AuditoriaJpaRepositorio auditorias;
    @Autowired PedidoServico pedidoServico;
    @Autowired com.raizes.raizes_api.aplicacao.servico.EstoqueServico estoqueServico;

    String enviar(String rota, String corpo, String token, int statusEsperado) throws Exception {
        var req = post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo);
        if (token != null) req.header("Authorization", "Bearer " + token);
        return mvc.perform(req).andExpect(status().is(statusEsperado)).andReturn().getResponse().getContentAsString();
    }
    String campo(String corpo, String campo) { return json.readTree(corpo).get(campo).asText(); }
    String admin() throws Exception {
        return campo(enviar("/auth/login", "{\"email\":\"admin@teste.local\",\"senha\":\"SenhaTeste123\"}", null, 200), "accessToken");
    }
    String cliente() throws Exception {
        String email = UUID.randomUUID() + "@teste.local";
        enviar("/usuarios", "{\"nome\":\"Cliente\",\"email\":\"" + email + "\",\"senha\":\"SenhaTeste123\",\"consentimentoLgpd\":true}", null, 201);
        return campo(enviar("/auth/login", "{\"email\":\"" + email + "\",\"senha\":\"SenhaTeste123\"}", null, 200), "accessToken");
    }
    record Cenario(String admin, String cliente, String unidade, String produto) {}
    Cenario preparar(int quantidade, String preco) throws Exception {
        String a = admin();
        String u = campo(enviar("/unidades", "{\"nome\":\"Unidade\",\"endereco\":\"Rua teste\"}", a, 201), "id");
        String p = campo(enviar("/produtos", "{\"nome\":\"Produto\",\"descricao\":\"Teste\",\"preco\":" + preco + "}", a, 201), "id");
        enviar("/estoques/entradas", movimento(u, p, quantidade), a, 200);
        return new Cenario(a, cliente(), u, p);
    }
    String movimento(String u, String p, int q) { return "{\"unidadeId\":\""+u+"\",\"produtoId\":\""+p+"\",\"quantidade\":"+q+"}"; }
    String corpoPedido(Cenario c, String canal, int quantidade) {
        return "{\"unidadeId\":\""+c.unidade()+"\",\"canalPedido\":\""+canal+"\",\"formaPagamento\":\"MOCK\",\"itens\":[{\"produtoId\":\""+c.produto()+"\",\"quantidade\":"+quantidade+"}]}";
    }
    String criar(Cenario c, String canal) throws Exception { return enviar("/pedidos", corpoPedido(c, canal, 2), c.cliente(), 201); }
    int estoque(Cenario c) { return estoques.findByUnidadeIdAndProdutoId(UUID.fromString(c.unidade()), UUID.fromString(c.produto())).orElseThrow().getQuantidadeDisponivel(); }

    @ParameterizedTest @ValueSource(strings={"APP","WEB","BALCAO","TOTEM","PICKUP"})
    void calculaDescontoPorCanal(String canal) throws Exception {
        Cenario c = preparar(10, "29.90");
        var resposta = json.readTree(criar(c, canal));
        boolean promocional = canal.equals("APP") || canal.equals("WEB");
        assertEquals(59.80, resposta.get("subtotal").asDouble(), 0.001);
        assertEquals(promocional ? 5.98 : 0, resposta.get("valorDesconto").asDouble(), 0.001);
        assertEquals(promocional ? 53.82 : 59.80, resposta.get("total").asDouble(), 0.001);
        assertEquals(8, estoque(c));
    }
    @Test void arredondaDesconto() throws Exception {
        Cenario c = preparar(1,"10.05");
        var resposta = json.readTree(enviar("/pedidos",corpoPedido(c,"APP",1),c.cliente(),201));
        assertEquals(1.01,resposta.get("valorDesconto").asDouble(),0.001);
        assertEquals(9.04,resposta.get("total").asDouble(),0.001);
    }
    @Test void pagamentosExigemTitularidadeERegistramAutor() throws Exception {
        Cenario c = preparar(10,"29.90"); String id = campo(criar(c,"APP"),"id"); String outro = cliente();
        enviar("/pagamentos/pedidos/"+id,"{\"metodo\":\"MOCK\",\"aprovadoMock\":true}",outro,403);
        mvc.perform(get("/pagamentos/pedidos/"+id).header("Authorization","Bearer "+outro)).andExpect(status().isForbidden());
        assertTrue(pagamentos.findByPedidoId(UUID.fromString(id)).isEmpty());
        String pag = enviar("/pagamentos/pedidos/"+id,"{\"metodo\":\"MOCK\",\"aprovadoMock\":true}",c.admin(),200);
        assertEquals("APROVADO",campo(pag,"status"));
        var pedido = pedidos.findById(UUID.fromString(id)).orElseThrow();
        assertEquals("PAGO",pedido.getStatus().name());
        assertEquals("53.82",pedido.getTotal().toPlainString());
        assertEquals("5.98",pedido.getValorDesconto().toPlainString());
        mvc.perform(get("/pedidos/"+id).header("Authorization","Bearer "+c.cliente())).andExpect(jsonPath("$.itens.length()").value(1));
        UUID adminId = UUID.fromString(campo(enviar("/auth/login","{\"email\":\"admin@teste.local\",\"senha\":\"SenhaTeste123\"}",null,200),"usuarioId"));
        assertTrue(auditorias.findAll().stream().anyMatch(a -> a.getRecursoId().toString().equals(campo(pag,"id")) && a.getUsuarioId().equals(adminId)));
        enviar("/pagamentos/pedidos/"+id,"{\"metodo\":\"MOCK\",\"aprovadoMock\":true}",c.cliente(),409);
        mvc.perform(get("/fidelidade/saldo").header("Authorization","Bearer "+c.cliente())).andExpect(jsonPath("$.pontos").value(53));
    }
    @Test void recusaMantemPedidoENaoGeraPontos() throws Exception {
        Cenario c=preparar(10,"10.00");String id=campo(criar(c,"APP"),"id");
        enviar("/pagamentos/pedidos/"+id,"{\"metodo\":\"MOCK\",\"aprovadoMock\":false}",c.cliente(),200);
        assertEquals("AGUARDANDO_PAGAMENTO",pedidos.findById(UUID.fromString(id)).orElseThrow().getStatus().name());
        mvc.perform(get("/fidelidade/saldo").header("Authorization","Bearer "+c.cliente())).andExpect(jsonPath("$.pontos").value(0));
        enviar("/pagamentos/pedidos/"+id,"{\"metodo\":\"MOCK\",\"aprovadoMock\":true}",c.cliente(),409);
    }
    @Test void statusExigeOperadorETransicaoValida() throws Exception {
        Cenario c=preparar(10,"10.00");String id=campo(criar(c,"APP"),"id");
        mvc.perform(patch("/pedidos/"+id+"/status").header("Authorization","Bearer "+c.cliente()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELADO\"}")).andExpect(status().isForbidden()).andExpect(jsonPath("$.erro").value("ACESSO_NEGADO"));
        alterar(c,id,"EM_PREPARO",409);
        enviar("/pagamentos/pedidos/"+id,"{\"metodo\":\"MOCK\",\"aprovadoMock\":true}",c.cliente(),200);
        alterar(c,id,"PRONTO",409);alterar(c,id,"EM_PREPARO",200);alterar(c,id,"PRONTO",200);alterar(c,id,"ENTREGUE",200);alterar(c,id,"CANCELADO",409);
    }
    void alterar(Cenario c,String id,String statusPedido,int codigo) throws Exception {
        mvc.perform(patch("/pedidos/"+id+"/status").header("Authorization","Bearer "+c.admin()).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\""+statusPedido+"\"}")).andExpect(status().is(codigo));
    }
    @Test void falhaNoSegundoItemDesfazPrimeiraBaixa() throws Exception {
        Cenario c=preparar(10,"10.00");String corpo=corpoPedido(c,"APP",2);
        corpo=corpo.replace("}]}","},{\"produtoId\":\""+UUID.randomUUID()+"\",\"quantidade\":1}]}");
        enviar("/pedidos",corpo,c.cliente(),404);assertEquals(10,estoque(c));
    }
    @Test void validaJsonEnumUuidECampos() throws Exception {
        enviar("/usuarios","{",null,400);
        enviar("/usuarios","{}",null,422);
        mvc.perform(get("/produtos/invalido")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.erro").value("PARAMETRO_INVALIDO"));
        Cenario c=preparar(5,"10.00");
        enviar("/pedidos",corpoPedido(c,"DESCONHECIDO",1),c.cliente(),400);
        enviar("/pedidos",corpoPedido(c,"APP",0),c.cliente(),422);
        enviar("/pedidos",corpoPedido(c,"APP",1).replaceAll("\\[.*\\]","[null]"),c.cliente(),422);
        enviar("/usuarios/internos","{\"nome\":\"Gerente\",\"email\":\"g@teste.local\",\"senha\":\"SenhaTeste123\",\"perfil\":\"GERENTE\",\"consentimentoLgpd\":true}",c.admin(),422);
        mvc.perform(get("/pedidos")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.erro").value("NAO_AUTENTICADO"));
    }
    @Test void loginInvalidoEJwtInvalido() throws Exception {
        enviar("/auth/login","{\"email\":\"admin@teste.local\",\"senha\":\"errada\"}",null,401);
        mvc.perform(get("/pedidos").header("Authorization","Bearer invalido")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.erro").value("NAO_AUTENTICADO"));
        var login=json.readTree(enviar("/auth/login","{\"email\":\"admin@teste.local\",\"senha\":\"SenhaTeste123\"}",null,200));
        assertEquals(3600,login.get("expiresIn").asLong());
    }
    @Test void mantemProdutosUnidadesEImpedeVendaDeProdutoInativo() throws Exception {
        Cenario c=preparar(5,"10.00");
        mvc.perform(put("/produtos/"+c.produto()).header("Authorization","Bearer "+c.cliente())
                .contentType("application/json").content("{\"nome\":\"Novo\",\"descricao\":\"Teste\",\"preco\":12.00,\"ativo\":true}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/produtos/"+c.produto()).header("Authorization","Bearer "+c.admin())
                .contentType("application/json").content("{\"nome\":\"Novo\",\"descricao\":\"Teste\",\"preco\":12.00,\"ativo\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.preco").value(12.00));
        mvc.perform(put("/unidades/"+c.unidade()).header("Authorization","Bearer "+c.admin())
                .contentType("application/json").content("{\"nome\":\"Nova unidade\",\"endereco\":\"Outra rua\",\"ativa\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Nova unidade"));
        mvc.perform(delete("/produtos/"+c.produto()).header("Authorization","Bearer "+c.admin())).andExpect(status().isNoContent());
        mvc.perform(get("/produtos/"+c.produto())).andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        enviar("/pedidos",corpoPedido(c,"APP",1),c.cliente(),409);assertEquals(5,estoque(c));
        mvc.perform(delete("/unidades/"+c.unidade()).header("Authorization","Bearer "+c.admin())).andExpect(status().isNoContent());
        mvc.perform(get("/unidades/"+c.unidade())).andExpect(status().isOk()).andExpect(jsonPath("$.ativa").value(false));
    }

    @Test void primeiraEntradaConcorrenteAcumulaSemDuplicar() throws Exception {
        String a=admin();
        String u=campo(enviar("/unidades","{\"nome\":\"Unidade\",\"endereco\":\"Rua\"}",a,201),"id");
        String p=campo(enviar("/produtos","{\"nome\":\"Produto\",\"descricao\":\"Teste\",\"preco\":10.00}",a,201),"id");
        var req=json.readValue(movimento(u,p,5),com.raizes.raizes_api.api.dto.requisicao.MovimentarEstoqueRequisicao.class);
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch inicio=new CountDownLatch(1);
        Callable<Void> entrar=()->{inicio.await();estoqueServico.registrarEntrada(req);return null;};
        try {
            var x=pool.submit(entrar);var y=pool.submit(entrar);inicio.countDown();x.get(15,TimeUnit.SECONDS);y.get(15,TimeUnit.SECONDS);
            assertEquals(10,estoques.findByUnidadeIdAndProdutoId(UUID.fromString(u),UUID.fromString(p)).orElseThrow().getQuantidadeDisponivel());
            assertEquals(1,estoques.findByUnidadeId(UUID.fromString(u)).size());
        } finally {pool.shutdownNow();}
    }

    @Test void comprasConcorrentesNaoVendemMesmoEstoque() throws Exception {
        Cenario c=preparar(1,"10.00");
        var requisicao=json.readValue(corpoPedido(c,"APP",1),CriarPedidoRequisicao.class);
        ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch inicio=new CountDownLatch(1);
        Callable<Boolean> comprar=() -> {inicio.await();try {pedidoServico.criar(UUID.randomUUID(),requisicao);return true;} catch(com.raizes.raizes_api.dominio.excecao.EstoqueInsuficienteExcecao ex){return false;}};
        try {
            Future<Boolean> a=pool.submit(comprar),b=pool.submit(comprar);inicio.countDown();
            assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));assertEquals(0,estoque(c));
        } finally {pool.shutdownNow();}
    }
}
