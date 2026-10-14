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
    static final tools.jackson.databind.ObjectMapper mockJson = new tools.jackson.databind.ObjectMapper();
    static final java.util.concurrent.ConcurrentHashMap<String,Integer> errosProvedor = new java.util.concurrent.ConcurrentHashMap<>();
    static final java.util.concurrent.ConcurrentHashMap<String,String> payloadsEnviados = new java.util.concurrent.ConcurrentHashMap<>();
    static final java.util.concurrent.ConcurrentHashMap<String,java.util.concurrent.atomic.AtomicInteger> chamadasProvedor = new java.util.concurrent.ConcurrentHashMap<>();
    static com.sun.net.httpserver.HttpServer servidorMock;

    @org.springframework.test.context.DynamicPropertySource
    static void configurarProvedor(org.springframework.test.context.DynamicPropertyRegistry registry) throws Exception {
        servidorMock = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1",0),0);
        servidorMock.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
        servidorMock.createContext("/pagamentos", exchange -> {
            String corpo = new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            var entrada = mockJson.readTree(corpo);
            String id = entrada.get("pedidoId").asText();
            payloadsEnviados.put(id,corpo);
            chamadasProvedor.computeIfAbsent(id,k->new java.util.concurrent.atomic.AtomicInteger()).incrementAndGet();
            int statusHttp = errosProvedor.getOrDefault(id,201);
            String retorno = mockJson.writeValueAsString(java.util.Map.of(
                    "pedidoId",id,"codigoTransacao","MOCK-"+UUID.randomUUID(),
                    "status",entrada.get("aprovadoMock").asBoolean() ? "APROVADO" : "RECUSADO",
                    "valor",entrada.get("valor").decimalValue(),"moeda","BRL",
                    "mensagem","Retorno do provedor mock HTTP"));
            if (!(id+":"+entrada.get("numeroTentativa").asInt()).equals(exchange.getRequestHeaders().getFirst("Idempotency-Key"))) statusHttp=400;
            byte[] resposta=retorno.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(statusHttp,resposta.length);
            try(var out=exchange.getResponseBody()){out.write(resposta);}
        });
        servidorMock.start();
        registry.add("app.pagamento.mock.url",()->"http://127.0.0.1:"+servidorMock.getAddress().getPort()+"/pagamentos");
    }

    @org.junit.jupiter.api.AfterAll
    static void encerrarProvedor() { if(servidorMock!=null) servidorMock.stop(0); }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired EstoqueJpaRepositorio estoques;
    @Autowired PedidoJpaRepositorio pedidos;
    @Autowired PagamentoJpaRepositorio pagamentos;
    @Autowired AuditoriaJpaRepositorio auditorias;
    @Autowired ProdutoUnidadeJpaRepositorio cardapios;
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
        configurarCardapio(a, u, p, true, 200);
        return new Cenario(a, cliente(), u, p);
    }
    void configurarCardapio(String token, String unidade, String produto, boolean ativo, int codigo) throws Exception {
        mvc.perform(put("/unidades/"+unidade+"/cardapio/produtos/"+produto)
                .header("Authorization", "Bearer "+token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"ativo\":"+ativo+"}"))
                .andExpect(status().is(codigo));
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
        assertTrue(pagamentos.findFirstByPedidoIdOrderByNumeroTentativaDesc(UUID.fromString(id)).isEmpty());
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
        mvc.perform(get("/fidelidade/saldo").header("Authorization","Bearer "+c.cliente())).andExpect(jsonPath("$.restante").value(53));
    }
    @Test void recusaMantemPedidoENaoGeraPontos() throws Exception {
        Cenario c=preparar(10,"10.00");String id=campo(criar(c,"APP"),"id");
        enviar("/pagamentos/pedidos/"+id,"{\"metodo\":\"MOCK\",\"aprovadoMock\":false}",c.cliente(),200);
        assertEquals("AGUARDANDO_PAGAMENTO",pedidos.findById(UUID.fromString(id)).orElseThrow().getStatus().name());
        mvc.perform(get("/fidelidade/saldo").header("Authorization","Bearer "+c.cliente())).andExpect(jsonPath("$.restante").value(0));
        enviar("/pagamentos/pedidos/"+id,"{\"metodo\":\"MOCK\",\"aprovadoMock\":true}",c.cliente(),200);
        assertEquals(2,pagamentos.findByPedidoIdOrderByNumeroTentativaDesc(UUID.fromString(id)).size());
        assertEquals("PAGO",pedidos.findById(UUID.fromString(id)).orElseThrow().getStatus().name());
        mvc.perform(get("/pagamentos/pedidos/"+id+"/tentativas").header("Authorization","Bearer "+c.cliente()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].numeroTentativa").value(2))
                .andExpect(jsonPath("$[0].status").value("APROVADO"))
                .andExpect(jsonPath("$[1].status").value("RECUSADO"));
        mvc.perform(get("/pagamentos/pedidos/"+id+"/tentativas").header("Authorization","Bearer "+cliente()))
                .andExpect(status().isForbidden());
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

    @Test void cardapioIsolaUnidadesEAtualizaDisponibilidadeAposVenda() throws Exception {
        Cenario c=preparar(1,"10.00");
        String outra=campo(enviar("/unidades","{\"nome\":\"Outra\",\"endereco\":\"Rua\"}",c.admin(),201),"id");
        enviar("/estoques/entradas",movimento(outra,c.produto(),5),c.admin(),200);
        mvc.perform(get("/unidades/"+outra+"/cardapio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/unidades/"+c.unidade()+"/cardapio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].produtoId").value(c.produto()))
                .andExpect(jsonPath("$[0].nome").value("Produto"))
                .andExpect(jsonPath("$[0].descricao").value("Teste"))
                .andExpect(jsonPath("$[0].preco").value(10.00))
                .andExpect(jsonPath("$[0].disponivel").value(true));
        enviar("/pedidos",corpoPedido(c,"APP",1),c.cliente(),201);
        mvc.perform(get("/unidades/"+c.unidade()+"/cardapio"))
                .andExpect(jsonPath("$[0].disponivel").value(false));
        enviar("/pedidos",corpoPedido(c,"APP",1),c.cliente(),409);
        enviar("/estoques/entradas",movimento(c.unidade(),c.produto(),1),c.admin(),200);
        mvc.perform(get("/unidades/"+c.unidade()+"/cardapio"))
                .andExpect(jsonPath("$[0].disponivel").value(true));
    }

    @Test void produtoSemEstoqueApareceIndisponivelNoCardapio() throws Exception {
        Cenario c=preparar(1,"10.00");
        String outra=campo(enviar("/unidades","{\"nome\":\"Outra\",\"endereco\":\"Rua\"}",c.admin(),201),"id");
        configurarCardapio(c.admin(),outra,c.produto(),true,200);
        mvc.perform(get("/unidades/"+outra+"/cardapio")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].disponivel").value(false));
        assertTrue(estoques.findByUnidadeIdAndProdutoId(UUID.fromString(outra),UUID.fromString(c.produto())).isEmpty());
    }

    @Test void desabilitarProdutoImpedeVendaApenasNaUnidadeEscolhida() throws Exception {
        Cenario c=preparar(5,"10.00");
        String outra=campo(enviar("/unidades","{\"nome\":\"Outra\",\"endereco\":\"Rua\"}",c.admin(),201),"id");
        configurarCardapio(c.admin(),outra,c.produto(),true,200);
        enviar("/estoques/entradas",movimento(outra,c.produto(),5),c.admin(),200);
        configurarCardapio(c.admin(),c.unidade(),c.produto(),false,200);
        mvc.perform(get("/unidades/"+c.unidade()+"/cardapio"))
                .andExpect(jsonPath("$.length()").value(0));
        enviar("/pedidos",corpoPedido(c,"APP",1),c.cliente(),409);
        assertEquals(5,estoque(c));
        enviar("/pedidos",corpoPedido(c,"APP",1).replace(c.unidade(),outra),c.cliente(),201);
        configurarCardapio(c.admin(),c.unidade(),c.produto(),true,200);
        configurarCardapio(c.admin(),c.unidade(),c.produto(),true,200);
        enviar("/pedidos",corpoPedido(c,"APP",1),c.cliente(),201);
        assertEquals(2,cardapios.findAll().stream().filter(v -> v.getProdutoId().toString().equals(c.produto())).count());
    }

    @Test void produtoForaDoCardapioDesfazBaixaDeTodosOsItens() throws Exception {
        Cenario c=preparar(5,"10.00");
        String fora=campo(enviar("/produtos","{\"nome\":\"Fora\",\"descricao\":\"Teste\",\"preco\":10.00}",c.admin(),201),"id");
        enviar("/estoques/entradas",movimento(c.unidade(),fora,5),c.admin(),200);
        String corpo=corpoPedido(c,"APP",1).replace("}]}","},{\"produtoId\":\""+fora+"\",\"quantidade\":1}]}");
        enviar("/pedidos",corpo,c.cliente(),409);
        assertEquals(5,estoque(c));
        assertEquals(5,estoques.findByUnidadeIdAndProdutoId(UUID.fromString(c.unidade()),UUID.fromString(fora))
                .orElseThrow().getQuantidadeDisponivel());
    }

    @Test void cardapioOcultaProdutoInativoERejeitaUnidadeInativa() throws Exception {
        Cenario c=preparar(1,"10.00");
        mvc.perform(delete("/produtos/"+c.produto()).header("Authorization","Bearer "+c.admin()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/unidades/"+c.unidade()+"/cardapio"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(delete("/unidades/"+c.unidade()).header("Authorization","Bearer "+c.admin()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/unidades/"+c.unidade()+"/cardapio")).andExpect(status().isNotFound());
    }

    @Test void cardapioValidaIdsCorpoEAutenticacao() throws Exception {
        Cenario c=preparar(1,"10.00");
        mvc.perform(get("/unidades/"+UUID.randomUUID()+"/cardapio")).andExpect(status().isNotFound());
        mvc.perform(get("/unidades/invalida/cardapio")).andExpect(status().isBadRequest());
        configurarCardapio(c.admin(),UUID.randomUUID().toString(),c.produto(),true,404);
        configurarCardapio(c.admin(),c.unidade(),UUID.randomUUID().toString(),true,404);
        String rota="/unidades/"+c.unidade()+"/cardapio/produtos/"+c.produto();
        mvc.perform(put(rota).header("Authorization","Bearer "+c.admin()).contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(put(rota).contentType("application/json").content("{\"ativo\":true}"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest @ValueSource(strings={"ADMIN","GERENTE","CLIENTE","COZINHA","ATENDENTE"})
    void somenteAdminEGerenteConfiguramCardapio(String perfil) throws Exception {
        Cenario c=preparar(1,"10.00");
        boolean permitido=perfil.equals("ADMIN") || perfil.equals("GERENTE");
        mvc.perform(put("/unidades/"+c.unidade()+"/cardapio/produtos/"+c.produto())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()
                        .jwt(t -> t.subject(UUID.randomUUID().toString()).claim("perfil",perfil))
                        .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_"+perfil)))
                .contentType("application/json").content("{\"ativo\":false}"))
                .andExpect(status().is(permitido ? 200 : 403));
        assertEquals(!permitido,cardapios.existsByUnidadeIdAndProdutoIdAndAtivoTrue(
                UUID.fromString(c.unidade()),UUID.fromString(c.produto())));
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

    @Autowired com.raizes.raizes_api.aplicacao.servico.FidelidadeServico fidelidadeServico;
    @Autowired UsuarioJpaRepositorio usuariosFidelidade;
    @Autowired FidelidadeCreditoJpaRepositorio creditosFidelidade;

    void mudarConsentimento(Cenario c, UUID clienteId, boolean consentimento) throws Exception {
        var usuario=usuariosFidelidade.findById(clienteId).orElseThrow();
        mvc.perform(put("/usuarios/"+clienteId).header("Authorization","Bearer "+c.cliente())
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of(
                        "nome",usuario.getNome(),"email",usuario.getEmail(),"consentimentoLgpd",consentimento))))
                .andExpect(status().isOk());
    }

    @Test void fidelidadeRespeitaConsentimentoNoPagamentoESemCreditoRetroativo() throws Exception {
        Cenario c=preparar(10,"60.00");
        var primeiro=json.readTree(criar(c,"BALCAO"));
        UUID clienteId=UUID.fromString(primeiro.get("clienteId").asText());
        String pagamento=json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true));
        mudarConsentimento(c,clienteId,false);
        enviar("/pagamentos/pedidos/"+primeiro.get("id").asText(),pagamento,c.cliente(),200);
        assertEquals(0,fidelidadeServico.consultarSaldo(clienteId).pontos());
        enviar("/fidelidade/resgates","",c.cliente(),409);
        mudarConsentimento(c,clienteId,true);
        assertEquals(0,fidelidadeServico.consultarSaldo(clienteId).pontos());
        String segundo=campo(criar(c,"BALCAO"),"id");
        enviar("/pagamentos/pedidos/"+segundo,pagamento,c.cliente(),200);
        assertEquals(20,fidelidadeServico.consultarSaldo(clienteId).pontos());
        assertEquals(new java.math.BigDecimal("1.00"),fidelidadeServico.consultarSaldo(clienteId).saldoCredito());
        mudarConsentimento(c,clienteId,false);
        enviar("/fidelidade/resgates","",c.cliente(),409);
        assertEquals(20,fidelidadeServico.consultarSaldo(clienteId).pontos());
        assertEquals(new java.math.BigDecimal("1.00"),fidelidadeServico.consultarSaldo(clienteId).saldoCredito());
        mudarConsentimento(c,clienteId,true);
        enviar("/fidelidade/resgates","",c.cliente(),409);
        assertEquals(20,fidelidadeServico.consultarSaldo(clienteId).pontos());
        mvc.perform(get("/fidelidade/resgates").header("Authorization","Bearer "+c.cliente()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/fidelidade/resgates").header("Authorization","Bearer "+cliente()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test void resgatesConcorrentesNaoGastamMesmoSaldo() throws Exception {
        Cenario c=preparar(2,"60.00");
        var pedido=json.readTree(criar(c,"BALCAO"));
        UUID clienteId=UUID.fromString(pedido.get("clienteId").asText());
        enviar("/pagamentos/pedidos/"+pedido.get("id").asText(),
                json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true)),c.cliente(),200);
        // Simula pontos pendentes de conversao para verificar o endpoint manual.
        creditosFidelidade.save(new FidelidadeCreditoEntidade(clienteId,UUID.randomUUID(),new java.math.BigDecimal("100.00")));
        ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch inicio=new CountDownLatch(1);
        Callable<Boolean> resgatar=()->{inicio.await();try {fidelidadeServico.resgatar(clienteId);return true;}
            catch(com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao e){return false;}};
        try {
            var a=pool.submit(resgatar);var b=pool.submit(resgatar);inicio.countDown();
            assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));
            assertEquals(20,fidelidadeServico.consultarSaldo(clienteId).pontos());
            assertEquals(2,fidelidadeServico.listarResgates(clienteId).size());
        } finally {pool.shutdownNow();}
    }

    @Test void creditoAutomaticoPodePagarPedidoInteiroSemGerarNovosPontos() throws Exception {
        Cenario c=preparar(10,"100.00");
        var pedido=json.readTree(enviar("/pedidos",corpoPedido(c,"BALCAO",1),c.cliente(),201));
        UUID id=UUID.fromString(pedido.get("clienteId").asText());
        enviar("/pagamentos/pedidos/"+pedido.get("id").asText(),
                json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true)),c.cliente(),200);
        assertEquals(0,fidelidadeServico.consultarSaldo(id).pontos());
        assertEquals(new java.math.BigDecimal("1.00"),fidelidadeServico.consultarSaldo(id).saldoCredito());
        mvc.perform(put("/produtos/"+c.produto()).header("Authorization","Bearer "+c.admin()).contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("nome","Produto","descricao","Teste","preco",1,"ativo",true))))
                .andExpect(status().isOk());
        String proximo=campo(enviar("/pedidos",corpoPedido(c,"BALCAO",1),c.cliente(),201),"id");
        var pagamento=json.readTree(enviar("/pagamentos/pedidos/"+proximo,
                json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true,"valorFidelidade",1)),c.cliente(),200));
        assertEquals(1,pagamento.get("valor").asDouble(),0.001);
        assertEquals(1,pagamento.get("valorFidelidade").asDouble(),0.001);
        assertEquals(0,pagamento.get("valorExterno").asDouble(),0.001);
        assertEquals(0,fidelidadeServico.consultarSaldo(id).pontos());
        assertEquals(new java.math.BigDecimal("0.00"),fidelidadeServico.consultarSaldo(id).saldoCredito());
        enviar("/pagamentos/pedidos/"+proximo,
                json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true,"valorFidelidade",1)),c.cliente(),409);
    }

    @Test void creditoNaoEConsumidoEmRecusaEValidaSaldoConsentimentoEValores() throws Exception {
        Cenario c=preparar(10,"60.00");
        var primeiro=json.readTree(criar(c,"BALCAO"));
        UUID id=UUID.fromString(primeiro.get("clienteId").asText());
        enviar("/pagamentos/pedidos/"+primeiro.get("id").asText(),
                json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true)),c.cliente(),200);
        String proximo=campo(criar(c,"BALCAO"),"id");
        String rota="/pagamentos/pedidos/"+proximo;
        enviar(rota,json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true,"valorFidelidade",2)),c.cliente(),409);
        enviar(rota,json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true,"valorFidelidade",121)),c.cliente(),409);
        enviar(rota,json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true,"valorFidelidade",-1)),c.cliente(),422);
        enviar(rota,json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true,"valorFidelidade",0.001)),c.cliente(),422);
        mudarConsentimento(c,id,false);
        enviar(rota,json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true,"valorFidelidade",1)),c.cliente(),409);
        mudarConsentimento(c,id,true);
        enviar(rota,json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",false,"valorFidelidade",1)),c.cliente(),200);
        assertEquals(new java.math.BigDecimal("1.00"),fidelidadeServico.consultarSaldo(id).saldoCredito());
        assertEquals(20,fidelidadeServico.consultarSaldo(id).pontos());
    }

    @Test void pagamentosConcorrentesNaoGastamMesmoCredito() throws Exception {
        Cenario c=preparar(10,"60.00");
        var primeiro=json.readTree(criar(c,"BALCAO"));
        UUID id=UUID.fromString(primeiro.get("clienteId").asText());
        enviar("/pagamentos/pedidos/"+primeiro.get("id").asText(),
                json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true)),c.cliente(),200);
        mvc.perform(put("/produtos/"+c.produto()).header("Authorization","Bearer "+c.admin()).contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("nome","Produto","descricao","Teste","preco",1,"ativo",true))))
                .andExpect(status().isOk());
        UUID a=UUID.fromString(campo(criar(c,"BALCAO"),"id"));
        UUID b=UUID.fromString(campo(criar(c,"BALCAO"),"id"));
        var req=json.readValue(json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true,"valorFidelidade",1)),
                com.raizes.raizes_api.api.dto.requisicao.ProcessarPagamentoRequisicao.class);
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch inicio=new CountDownLatch(1);
        java.util.function.Function<UUID,Boolean> pagar=pedidoId->{try {inicio.await();pagamentoServicoFidelidade.processar(pedidoId,req,id,"CLIENTE");return true;}
            catch(com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao e){return false;}
            catch(InterruptedException e){Thread.currentThread().interrupt();throw new RuntimeException(e);}};
        try {
            var x=pool.submit(()->pagar.apply(a));var y=pool.submit(()->pagar.apply(b));inicio.countDown();
            assertNotEquals(x.get(15,TimeUnit.SECONDS),y.get(15,TimeUnit.SECONDS));
            assertEquals(new java.math.BigDecimal("0.00"),fidelidadeServico.consultarSaldo(id).saldoCredito());
            assertEquals(21,fidelidadeServico.consultarSaldo(id).pontos());
        } finally {pool.shutdownNow();}
    }
    @Autowired com.raizes.raizes_api.aplicacao.servico.PagamentoServico pagamentoServicoFidelidade;

    @Test void pagamentoEnviaCanalEValorAoServicoEDevolvePayloadRegistrado() throws Exception {
        Cenario c=preparar(10,"29.90");
        String id=campo(criar(c,"TOTEM"),"id");
        String resposta=enviar("/pagamentos/pedidos/"+id,
                json.writeValueAsString(java.util.Map.of("metodo","PIX","aprovadoMock",true)),c.cliente(),200);
        var retorno=json.readTree(resposta);
        assertEquals("TOTEM",json.readTree(payloadsEnviados.get(id)).get("canalPedido").asText());
        assertEquals(59.80,retorno.get("solicitacaoServico").get("valor").asDouble(),0.001);
        assertEquals("BRL",retorno.get("retornoServico").get("moeda").asText());
        assertEquals(201,retorno.get("statusHttpServico").asInt());
        assertEquals("APROVADO",retorno.get("retornoServico").get("status").asText());
        assertEquals(retorno.get("codigoTransacaoMock"),retorno.get("retornoServico").get("codigoTransacao"));
        mvc.perform(get("/pagamentos/pedidos/"+id).header("Authorization","Bearer "+c.cliente()))
                .andExpect(status().isOk()).andExpect(content().json(resposta));
        assertNotNull(pagamentos.findFirstByPedidoIdOrderByNumeroTentativaDesc(UUID.fromString(id)).orElseThrow().getSolicitacaoServico());
    }

    @Test void falhaDoProvedorNaoRegistraPagamentoNemCreditaPontosEPermiteRepetir() throws Exception {
        Cenario c=preparar(10,"60.00");
        String id=campo(criar(c,"WEB"),"id");
        errosProvedor.put(id,503);
        String corpo=json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true));
        enviar("/pagamentos/pedidos/"+id,corpo,c.cliente(),502);
        assertTrue(pagamentos.findFirstByPedidoIdOrderByNumeroTentativaDesc(UUID.fromString(id)).isEmpty());
        assertEquals("AGUARDANDO_PAGAMENTO",pedidos.findById(UUID.fromString(id)).orElseThrow().getStatus().name());
        assertEquals(0,fidelidadeServico.consultarSaldo(pedidos.findById(UUID.fromString(id)).orElseThrow().getClienteId()).pontosGanhos());
        errosProvedor.remove(id);
        enviar("/pagamentos/pedidos/"+id,corpo,c.cliente(),200);
    }

    @Test void duasAprovacoesConcorrentesDoMesmoPedidoGeramUmaUnicaCobranca() throws Exception {
        Cenario c=preparar(10,"10.00");
        var pedido=json.readTree(criar(c,"APP"));
        UUID pedidoId=UUID.fromString(pedido.get("id").asText());
        UUID clienteId=UUID.fromString(pedido.get("clienteId").asText());
        var req=json.readValue(json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",true)),
                com.raizes.raizes_api.api.dto.requisicao.ProcessarPagamentoRequisicao.class);
        ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch inicio=new CountDownLatch(1);
        Callable<Boolean> pagar=()->{inicio.await();try {
            pagamentoServicoFidelidade.processar(pedidoId,req,clienteId,"CLIENTE");return true;
        } catch(com.raizes.raizes_api.dominio.excecao.RegraDeNegocioExcecao e){return false;}};
        try {
            var a=pool.submit(pagar);var b=pool.submit(pagar);inicio.countDown();
            assertNotEquals(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS));
            assertEquals(1,pagamentos.findByPedidoIdOrderByNumeroTentativaDesc(pedidoId).size());
            assertEquals(1,chamadasProvedor.get(pedidoId.toString()).get());
            assertEquals(18,fidelidadeServico.consultarSaldo(clienteId).pontosGanhos());
        } finally {pool.shutdownNow();}
    }

    @Test void recusasConsecutivasMantemHistoricoEAprovacaoFinalCreditaUmaVez() throws Exception {
        Cenario c=preparar(10,"10.00");String id=campo(criar(c,"APP"),"id");
        String recusa=json.writeValueAsString(java.util.Map.of("metodo","MOCK","aprovadoMock",false));
        enviar("/pagamentos/pedidos/"+id,recusa,c.cliente(),200);
        enviar("/pagamentos/pedidos/"+id,recusa,c.cliente(),200);
        mvc.perform(get("/pagamentos/pedidos/"+id).header("Authorization","Bearer "+c.cliente()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.numeroTentativa").value(2))
                .andExpect(jsonPath("$.status").value("RECUSADO"));
        enviar("/pagamentos/pedidos/"+id,
                json.writeValueAsString(java.util.Map.of("metodo","PIX","aprovadoMock",true)),c.cliente(),200);
        var historico=pagamentos.findByPedidoIdOrderByNumeroTentativaDesc(UUID.fromString(id));
        assertEquals(3,historico.size());
        assertEquals(3,historico.getFirst().getNumeroTentativa());
        assertEquals(18,fidelidadeServico.consultarSaldo(pedidos.findById(UUID.fromString(id)).orElseThrow().getClienteId()).pontosGanhos());
    }
}
