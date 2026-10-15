# Documentação de Casos de Uso — UC01 a UC16

**Sistema:** API Raízes do Nordeste.  
**Referência:** numeração do diagrama apresentado pelo autor.  
**Escopo:** comportamento atualmente implementado na API, incluindo o gateway mock.

Os identificadores são únicos e não indicam ordem de execução. Nesta documentação,
**Realizar Pedido é UC05** e **Solicitar Pagamento é UC15**. As funcionalidades
críticas detalhadas no final seguem esses mesmos números.

## 1. Atores

| Ator do diagrama | Significado |
|---|---|
| Cliente (App/Web/Totem) | Usuário final que utiliza os canais digitais. App/Web/Totem são interfaces, não perfis distintos. |
| Atendente (Balcão) | Operador presencial, com perfil ATENDENTE. |
| Cozinha | Operador com perfil COZINHA. |
| Gerente / Administrador | Agrupamento visual de dois perfis: GERENTE e ADMIN. Não significa permissões idênticas. |
| Sistemas Externos de Pagamento (Gateway) | Serviço externo participante dos pagamentos/estornos. Na implementação é um mock HTTP local separado. |

PICKUP também é aceito em `canalPedido`, embora não seja um ator separado no desenho.
A API não implementa as interfaces gráficas desses canais.

## 2. Catálogo e associações entre atores e casos de uso

**Notação:** associação é uma linha contínua entre ator e elipse, normalmente sem seta.
Ela indica participação/comunicação, não ordem de execução. Atores podem se associar
a vários casos, e vários atores podem participar do mesmo caso.

| Caso | Nome | Associações e explicação |
|---|---|---|
| UC01 | Cadastrar-se | Cliente; acesso público. |
| UC02 | Autenticar-se | Cliente, Atendente, Cozinha, Gerente e Administrador. |
| UC03 | Consultar fidelidade | Cliente; demais usuários consultam sua própria conta; ADMIN também pode consultar outra conta. |
| UC04 | Consultar unidades e cardápio | Cliente; disponível publicamente aos demais atores humanos. |
| UC05 | Realizar pedido | Cliente e Atendente; tecnicamente qualquer usuário autenticado, incluindo Gerente, ADMIN e Cozinha. |
| UC06 | Validar e baixar estoque | Comportamento interno da API, incluído por UC05. |
| UC07 | Usar crédito de fidelidade | Cliente titular do pedido; Atendente, Gerente e ADMIN ao processar seu pagamento. |
| UC08 | Acompanhar pedido | Cliente; Atendente, Cozinha, Gerente e ADMIN. |
| UC09 | Solicitar estorno | Gateway mock como ator de apoio; iniciado internamente por UC16. |
| UC10 | Gerenciar usuários internos | Somente Administrador (ADMIN). |
| UC11 | Gerenciar produtos, unidades e cardápios | Gerente e Administrador. |
| UC12 | Movimentar estoque | Gerente e Administrador. |
| UC13 | Atualizar o status do pedido | Cozinha, Atendente, Gerente e Administrador. |
| UC14 | Integrar pagamento | Gateway mock como ator de apoio; execução interna incluída em UC15. |
| UC15 | Solicitar pagamento | Cliente titular; Atendente, Gerente e Administrador; Gateway como apoio por UC14. |
| UC16 | Cancelar pedido | Atendente, Cozinha, Gerente e Administrador; expiração como disparo temporal interno. |

### Como desenhar as associações

| Origem | Destino(s) | Explicação |
|---|---|---|
| Cliente | UC01, UC02, UC03, UC04, UC05, UC07, UC08, UC15 | Fluxo principal do usuário final. UC07 só participa se houver crédito escolhido. |
| Atendente | UC02, UC04, UC05, UC07, UC08, UC13, UC15, UC16 | Operação do balcão. O cancelamento é iniciado em UC16, não em uma rota de estorno independente. |
| Cozinha | UC02, UC08, UC13, UC16 | Acompanhamento e operação. No backend também consegue UC05, por ser autenticada, mas não consegue UC15. |
| Gerente / Administrador | UC02, UC04, UC05, UC07, UC08, UC11, UC12, UC13, UC15, UC16 | Operações compartilhadas de gestão e atendimento. |
| Administrador, dentro do agrupamento | UC10; UC03 de outro usuário | São exceções exclusivas de ADMIN. Indicar isso em nota ou separar os dois atores no desenho. |
| Gateway | UC14, UC09 | Recebe solicitações externas de pagamento e estorno. Não cadastra usuários nem realiza pedidos. |

**Permissões adicionais:** por serem públicas, UC01 e UC04 podem ser acessadas pelos
demais atores humanos. Qualquer usuário autenticado pode consultar sua própria
fidelidade (UC03); essa consulta não precisa gerar várias linhas no diagrama.
UC05 também está liberado para COZINHA na implementação. As associações acima
privilegiam responsabilidades, mas essas permissões adicionais devem ser consideradas
se o desenho pretender representar toda a política de acesso.

Não associe o Cliente diretamente a UC06 ou UC14: são comportamentos internos
alcançados por inclusão. Não associe Atendente diretamente a UC09 como uma ação
independente: ele solicita UC16 e o sistema executa o estorno condicionalmente.

## 3. Relações entre casos de uso

| Origem da seta | Relação | Destino da seta | Condição e significado |
|---|---|---|---|
| UC05 — Realizar pedido | «include» | UC06 — Validar e baixar estoque | Etapa obrigatória de uma criação bem-sucedida. |
| UC15 — Solicitar pagamento | «include» | UC14 — Integrar pagamento | Toda solicitação válida executa a integração, inclusive se a parcela externa for zero. |
| UC07 — Usar crédito de fidelidade | «extend» | UC15 — Solicitar pagamento | Extensão quando `valorFidelidade > 0`, sujeita às validações. |
| UC09 — Solicitar estorno | «extend» | UC16 — Cancelar pedido | Extensão quando o pedido possui pagamento aprovado. |

Em ambas as dependências, use **linha tracejada com seta aberta**. Em «include», a
seta sai do caso que reutiliza o comportamento e aponta para o incluído. Em
«extend», sai da extensão e aponta para o caso base.

Não desenhe «include» de UC05 para UC15: a criação e o pagamento são chamadas
separadas, e um pedido pode permanecer aguardando pagamento. Não é necessário
incluir UC02 em todos os casos protegidos; o JWT válido é uma pré-condição.
A expiração é um disparo temporal de UC16, não um sexto ator humano.

## 4. Especificação de cada caso de uso

### UC01 — Cadastrar-se

**Objetivo:** Criar uma conta com perfil CLIENTE.

**Atores/participação:** Cliente; acesso público.

**Pré-condições:** Email ainda não cadastrado e dados válidos.

**Fluxo principal:**

1. Informar nome, email, senha e consentimento.
2. A API valida os campos e a disponibilidade do email.
3. A API aplica BCrypt à senha e grava o usuário como CLIENTE.
4. Retorna HTTP 201 com os dados públicos da conta.

**Pós-condições:** Conta criada; senha armazenada como hash; nenhum JWT é emitido por esse cadastro.

**Exceções:** Campos inválidos: 422; JSON inválido: 400; email já utilizado: 409.

**Rotas ou implementação:** POST /usuarios.

**Regras e associações:** Não permite escolher perfil interno. Consentimento false não impede cadastro ou compra, mas impede participação na fidelidade.

### UC02 — Autenticar-se

**Objetivo:** Obter um token JWT para operações protegidas.

**Atores/participação:** Cliente, Atendente, Cozinha, Gerente e Administrador.

**Pré-condições:** Conta existente e ativa.

**Fluxo principal:**

1. Informar email e senha.
2. A API consulta a conta e verifica seu estado.
3. Compara a senha com o hash BCrypt.
4. Retorna token, perfil e duração da sessão.

**Pós-condições:** Token emitido; nenhuma compra ou alteração de estoque ocorre.

**Exceções:** Credenciais incorretas: 401; usuário inativo: 409; campos inválidos: 422.

**Rotas ou implementação:** POST /auth/login.

**Regras e associações:** A duração padrão é 60 minutos. Autenticação é pré-condição dos demais casos protegidos, e não uma inclusão que obrigue novo login a cada operação.

### UC03 — Consultar fidelidade

**Objetivo:** Consultar pontos válidos, conversões líquidas, crédito disponível e histórico.

**Atores/participação:** Cliente; demais usuários consultam sua própria conta; ADMIN também pode consultar outra conta.

**Pré-condições:** JWT válido; conta consultada existente.

**Fluxo principal:**

1. Solicitar saldo ou histórico.
2. A API identifica o titular pelo token ou autoriza a consulta por ID.
3. Calcula pontos ainda válidos, conversões e saldo após pagamentos/estornos.
4. Retorna os dados solicitados.

**Pós-condições:** Saldo/histórico exibidos sem consumo de pontos ou crédito.

**Exceções:** Sem autenticação: 401; outra conta sem perfil ADMIN: 403; conta inexistente: 404.

**Rotas ou implementação:** GET /fidelidade/saldo; GET /fidelidade/saldo/{clienteId}; GET /fidelidade/resgates.

**Regras e associações:** Retirar consentimento não impede consultar o histórico. Saldo de crédito pode ficar negativo após reversão de crédito já gasto. Não confundir consulta com o uso de crédito de UC07.

### UC04 — Consultar unidades e cardápio

**Objetivo:** Escolher unidade e visualizar sua oferta de produtos.

**Atores/participação:** Cliente; disponível publicamente aos demais atores humanos.

**Pré-condições:** Para consultar cardápio: unidade existente e ativa. A consulta é pública.

**Fluxo principal:**

1. Consultar unidades.
2. Selecionar uma unidade ativa.
3. Solicitar seu cardápio.
4. A API retorna produtos ativos e habilitados no vínculo local, com preço e disponibilidade.

**Pós-condições:** Cardápio exibido; estoque permanece inalterado.

**Exceções:** Unidade inexistente/inativa no cardápio: 404. Unidade sem produtos habilitados: lista vazia.

**Rotas ou implementação:** GET /unidades; GET /unidades/{id}; GET /unidades/{unidadeId}/cardapio.

**Regras e associações:** Listagem geral de unidades inclui inativas. Produto sem saldo pode aparecer com disponivel=false. A disponibilidade precisa ser revalidada no UC06.

### UC05 — Realizar pedido

**Objetivo:** Registrar itens, valores, unidade e canal de um pedido.

**Atores/participação:** Cliente e Atendente; tecnicamente qualquer usuário autenticado, incluindo Gerente, ADMIN e Cozinha.

**Pré-condições:** JWT válido; unidade ativa; produtos ativos, habilitados no cardápio e com estoque suficiente.

**Fluxo principal:**

1. Informar unidade, canal, forma de pagamento e itens.
2. A API identifica o titular pelo JWT e valida os campos.
3. Executa UC06 para verificar e baixar o estoque local.
4. Calcula preços, subtotais, desconto e total no servidor.
5. Grava o pedido como AGUARDANDO_PAGAMENTO, com prazo e canal.
6. Registra auditoria e retorna HTTP 201.

**Pós-condições:** Pedido registrado; estoque baixado; pagamento ainda não realizado.

**Exceções:** Dados inválidos: 400/422; unidade/produto inexistente: 404; produto inativo ou fora do cardápio: 409; estoque insuficiente: 409. Falhas desfazem todas as baixas.

**Rotas ou implementação:** POST /pedidos.

**Regras e associações:** Inclui UC06. APP e WEB recebem 10% de desconto. O pagamento é separado em UC15. No balcão, a API atual usa o titular do token como clienteId; não permite escolher outro cliente na criação.

### UC06 — Validar e baixar estoque

**Objetivo:** Impedir venda sem disponibilidade e reservar os itens pela baixa do saldo local.

**Atores/participação:** Comportamento interno da API, incluído por UC05.

**Pré-condições:** Pedido em criação; unidade bloqueada na transação.

**Fluxo principal:**

1. Localizar estoque de cada produto na unidade.
2. Comparar saldo e quantidade solicitada.
3. Reduzir o saldo de cada item válido.
4. Confirmar as baixas junto ao pedido.

**Pós-condições:** Todos os itens baixados ou nenhuma baixa confirmada.

**Exceções:** Estoque inexistente: 404; insuficiente: 409. Falha em qualquer item causa rollback de todos.

**Rotas ou implementação:** Interno: PedidoServico.criar; não existe endpoint próprio de UC06..

**Regras e associações:** Cliente não movimenta estoque diretamente. Nova tentativa de pagamento não repete a baixa. Reposição posterior pertence ao cancelamento/expiração.

### UC07 — Usar crédito de fidelidade

**Objetivo:** Aplicar saldo de fidelidade como parte ou total do pagamento.

**Atores/participação:** Cliente titular do pedido; Atendente, Gerente e ADMIN ao processar seu pagamento.

**Pré-condições:** UC15 em execução; valorFidelidade positivo; titular do pedido ativo, com consentimento e saldo suficiente.

**Fluxo principal:**

1. Informar valorFidelidade em UC15.
2. A API bloqueia e valida o saldo do titular do pedido.
3. Calcula a parcela externa como total menos crédito.
4. Se UC14 retornar aprovação, debita o crédito na mesma transação.
5. Em recusa ou falha, preserva o saldo.

**Pós-condições:** Crédito consumido somente em aprovação; diferença paga pelo mock.

**Exceções:** Crédito negativo ou precisão inválida: 422; valor acima do saldo/total ou sem consentimento: 409.

**Rotas ou implementação:** POST /pagamentos/pedidos/{pedidoId}, campo valorFidelidade.

**Regras e associações:** Estende UC15 quando valorFidelidade > 0. Omitir esse campo equivale a zero. A parcela paga com fidelidade não gera novos pontos; mesmo com parcela externa zero, a implementação ainda chama o mock.

### UC08 — Acompanhar pedido

**Objetivo:** Consultar situação e valores dos pedidos e filtrar por canal/status.

**Atores/participação:** Cliente; Atendente, Cozinha, Gerente e ADMIN.

**Pré-condições:** JWT válido; pedido existente na consulta por ID.

**Fluxo principal:**

1. Solicitar lista ou detalhe.
2. A API limita CLIENTE aos próprios pedidos.
3. Aplica filtros opcionais de canal e status.
4. Retorna itens, valores, estado, prazo e informações de cancelamento.

**Pós-condições:** Pedidos consultados; nenhum estado alterado.

**Exceções:** Sem autenticação: 401; cliente acessando pedido alheio: 403; pedido inexistente: 404.

**Rotas ou implementação:** GET /pedidos; GET /pedidos/{id}; filtros canalPedido e status.

**Regras e associações:** Perfis operacionais atualmente têm visão de todas as unidades. Não há restrição por unidade do funcionário nesses métodos.

### UC09 — Solicitar estorno

**Objetivo:** Reverter o pagamento aprovado e os efeitos de fidelidade da compra cancelada.

**Atores/participação:** Gateway mock como ator de apoio; iniciado internamente por UC16.

**Pré-condições:** Cancelamento autorizado; pedido não finalizado; pagamento aprovado e reconhecido pelo mock.

**Fluxo principal:**

1. UC16 identifica pagamento aprovado.
2. A API envia identificação, código original, parcela externa e moeda ao mock.
3. O mock valida a transação e retorna ESTORNADO.
4. A API registra payloads/status HTTP e altera o pagamento para ESTORNADO.
5. Devolve crédito utilizado, reverte ganhos e recalcula conversões.

**Pós-condições:** Parcela externa estornada no mock; fidelidade ajustada; efeitos confirmados com o cancelamento.

**Exceções:** Mock indisponível/inconsistente ou sem transação original: 502; cancelamento e efeitos locais desfeitos. Repetição usa a mesma chave de estorno.

**Rotas ou implementação:** Interno à API Raízes; chamada HTTP POST /estornos no mock; resultado consultado em GET /pagamentos/pedidos/{pedidoId}..

**Regras e associações:** Estende UC16 somente se houver pagamento aprovado. Atendente não chama uma rota pública de estorno: solicita o cancelamento. A chave é pagamentoId:estorno. Crédito já gasto pode resultar em saldo negativo para compensação futura.

### UC10 — Gerenciar usuários internos

**Objetivo:** Cadastrar, consultar, atualizar e desativar colaboradores.

**Atores/participação:** Somente Administrador (ADMIN).

**Pré-condições:** JWT com perfil ADMIN; para cadastro, unidade existente e campos válidos.

**Fluxo principal:**

1. ADMIN escolhe a operação e informa dados.
2. A API verifica permissão, campos e regras de email/unidade.
3. Cadastra, lista, atualiza ou desativa o usuário.
4. Retorna o resultado sem expor a senha.

**Pós-condições:** Cadastro criado/alterado ou marcado inativo; exclusão não é física.

**Exceções:** Sem autenticação: 401; outro perfil, inclusive Gerente: 403; campos inválidos: 422; conflito de email/unidade no cadastro: 409; usuário inexistente na manutenção: 404.

**Rotas ou implementação:** POST/GET /usuarios/internos; PUT/DELETE /usuarios/internos/{id}.

**Regras e associações:** Cadastro aceita ADMIN, GERENTE, COZINHA e ATENDENTE e exige unidadeId. A listagem interna retorna GERENTE, COZINHA e ATENDENTE, sem incluir ADMIN. O PUT preserva senha, perfil, unidade e estado ativo.

### UC11 — Gerenciar produtos, unidades e cardápios

**Objetivo:** Manter o catálogo geral, as unidades e a habilitação local dos produtos.

**Atores/participação:** Gerente e Administrador.

**Pré-condições:** JWT com GERENTE ou ADMIN; referências existentes quando exigidas.

**Fluxo principal:**

1. Escolher produto, unidade ou vínculo de cardápio.
2. Informar dados da operação.
3. A API valida e salva ou desativa o registro.
4. Na configuração do cardápio, cria/atualiza vínculo ativo da unidade com o produto.

**Pós-condições:** Catálogo/unidade/vínculo atualizado; estoque não é aumentado automaticamente.

**Exceções:** Perfil indevido: 403; dados inválidos: 422; referências inexistentes: 404.

**Rotas ou implementação:** POST/PUT/DELETE /produtos; POST/PUT/DELETE /unidades; PUT /unidades/{unidadeId}/cardapio/produtos/{produtoId}.

**Regras e associações:** PUT/DELETE de produto/unidade usam /{id}. Exclusões são lógicas. Preço é geral do produto, não por unidade. Habilitação no cardápio e quantidade no estoque são controles distintos.

### UC12 — Movimentar estoque

**Objetivo:** Registrar entradas/saídas e consultar saldo por unidade.

**Atores/participação:** Gerente e Administrador.

**Pré-condições:** JWT com GERENTE ou ADMIN; unidade e produto existentes; quantidade positiva.

**Fluxo principal:**

1. Informar unidade, produto e quantidade ou consultar saldo.
2. A API bloqueia a unidade para movimentação.
3. Em saída, verifica saldo suficiente.
4. Atualiza quantidade e retorna o estoque.

**Pós-condições:** Saldo alterado ou consultado; cardápio não é habilitado automaticamente.

**Exceções:** Quantidade inválida: 422; referência/estoque inexistente: 404; saída acima do saldo: 409; perfil indevido: 403.

**Rotas ou implementação:** POST /estoques/entradas; POST /estoques/saidas; GET /estoques/total; GET /estoques/unidades/{unidadeId}; GET /estoques/unidades/{unidadeId}/produtos/{produtoId}.

**Regras e associações:** É diferente de UC06: aqui há uma solicitação administrativa explícita; UC06 é execução interna durante criação de pedido.

### UC13 — Atualizar o status do pedido

**Objetivo:** Avançar o atendimento operacional de pedido pago.

**Atores/participação:** Cozinha, Atendente, Gerente e Administrador.

**Pré-condições:** JWT operacional; pedido existente; transição permitida.

**Fluxo principal:**

1. Operador envia o novo status.
2. A API bloqueia o pedido e valida a transição.
3. Avança PAGO → EM_PREPARO → PRONTO → ENTREGUE.
4. Registra auditoria e devolve o pedido.

**Pós-condições:** Estado atualizado; ENTREGUE torna-se final.

**Exceções:** Cliente sem permissão: 403; inexistente: 404; salto, retorno ou mudança de estado final: 409.

**Rotas ou implementação:** PATCH /pedidos/{id}/status.

**Regras e associações:** PAGO é atribuído por pagamento aprovado, não por PATCH. Para CANCELADO, tratar UC16, apesar de compartilhar a rota. O backend permite as transições a todos os perfis operacionais; não impõe exclusividade por etapa.

### UC14 — Integrar pagamento

**Objetivo:** Trocar solicitação/retorno HTTP com o provedor e validar o resultado.

**Atores/participação:** Gateway mock como ator de apoio; execução interna incluída em UC15.

**Pré-condições:** UC15 autorizado e com valores definidos; serviço mock disponível.

**Fluxo principal:**

1. Montar payload com pedido, canal, método, BRL, valor externo, cenário e tentativa.
2. Enviar ao mock com chave idempotente.
3. Receber status HTTP, transação e APROVADO/RECUSADO.
4. Verificar coerência de pedido, valor, moeda e status.
5. Entregar o resultado a UC15 para registro e efeitos locais.

**Pós-condições:** Resultado validado e disponível para persistência por UC15.

**Exceções:** Conexão, timeout, erro HTTP ou payload inconsistente: 502 na API Raízes, sem confirmação local de pagamento/fidelidade.

**Rotas ou implementação:** Chamada externa POST /pagamentos no mock; adaptador ProvedorPagamentoMockHttp..

**Regras e associações:** Incluído por UC15. Gateway não usa a rota /pagamentos/pedidos/{pedidoId} como cliente. No mock, 201 indica criação e 200 repetição; aprovação/recusa são expressas no payload.

### UC15 — Solicitar pagamento

**Objetivo:** Pagar pedido e registrar cada tentativa com rastreabilidade.

**Atores/participação:** Cliente titular; Atendente, Gerente e Administrador; Gateway como apoio por UC14.

**Pré-condições:** Pedido AGUARDANDO_PAGAMENTO, dentro do prazo e sem aprovação anterior; autenticação e autorização válidas.

**Fluxo principal:**

1. Informar metodo, aprovadoMock e valorFidelidade opcional.
2. A API bloqueia o pedido e valida acesso, estado, prazo e valores.
3. Executa UC07 se houver crédito solicitado.
4. Define número da tentativa e executa UC14 obrigatoriamente.
5. Grava tentativa e payloads recebidos/enviados.
6. Se aprovado, debita crédito usado, gera ganhos elegíveis e altera pedido para PAGO.
7. Registra auditoria e retorna HTTP 200 com o resultado.

**Pós-condições:** Aprovação: pedido PAGO e fidelidade atualizada. Recusa: histórico preservado e pedido ainda aguardando pagamento.

**Exceções:** Outra conta ou Cozinha: 403; pedido inexistente: 404; já pago/cancelado/vencido ou crédito insuficiente: 409; falha no gateway: 502. Recusa confirmada usa HTTP 200 com RECUSADO.

**Rotas ou implementação:** POST /pagamentos/pedidos/{pedidoId}; GET /pagamentos/pedidos/{pedidoId}; GET /pagamentos/pedidos/{pedidoId}/tentativas.

**Regras e associações:** Inclui UC14; é estendido por UC07. Recusa permite novo número de tentativa. Aprovação impede nova cobrança. O estoque não é baixado novamente no pagamento.

### UC16 — Cancelar pedido

**Objetivo:** Encerrar pedido não entregue, tratando estoque, pagamento e fidelidade.

**Atores/participação:** Atendente, Cozinha, Gerente e Administrador; expiração como disparo temporal interno.

**Pré-condições:** Pedido existente, não CANCELADO/ENTREGUE; permissão operacional no cancelamento manual.

**Fluxo principal:**

1. Operador solicita CANCELADO, ou agendamento identifica pedido não pago vencido.
2. A API bloqueia o pedido e revalida o estado.
3. Antes do preparo, devolve quantidades ao estoque; após início, registra perda.
4. Se houver pagamento aprovado, executa UC09.
5. Grava CANCELADO, motivo, decisão de reposição e auditoria.

**Pós-condições:** Pedido finalizado; estoque reposto ou perda registrada; pagamento/fidelidade ajustados quando aplicável.

**Exceções:** Cliente sem permissão manual: 403; inexistente: 404; já finalizado: 409; estorno falha: 502 e rollback local.

**Rotas ou implementação:** PATCH /pedidos/{id}/status com CANCELADO; expiração por agendamento interno..

**Regras e associações:** UC09 estende este caso apenas em pedido pago. Expiração padrão: 15 minutos, verificação a cada 60 segundos, até 100 por execução. Não exige nem representa um ator humano adicional.

## 5. Feature obrigatória — F01: Realizar Pedido + Solicitar Pagamento

### Identificação, objetivo e participantes

- **Casos críticos:** UC05 e UC15, com participação de UC06, UC07 e UC14.
- **Objetivo:** registrar uma compra multicanal e confirmar seu pagamento com
  consistência de estoque, valores, tentativas e fidelidade.
- **Iniciadores:** Cliente ou Atendente; Gerente/ADMIN também possuem permissão.
- **Apoio:** Gateway mock, participante por UC14; UC09 atua em cancelamento pago posterior.
- **Gatilho:** confirmação dos itens e, em outra solicitação, confirmação do pagamento.

### Pré-condições

1. Usuário autenticado; unidade ativa.
2. Produtos ativos, habilitados no cardápio e com saldo suficiente naquela unidade.
3. Canal de origem informado e válido.
4. Para pagar: pedido aguardando pagamento, não vencido e sem aprovação anterior.
5. Para usar crédito: titular ativo, com consentimento e saldo suficiente.
6. Gateway disponível para concluir a operação; indisponibilidade segue o fluxo de exceção.

### Fluxo principal integrado

1. Ator consulta unidades e cardápio (UC04).
2. Seleciona itens e envia a criação do pedido (UC05).
3. API valida campos, unidade, produtos e vínculo com o cardápio.
4. Valida/baixa estoque da unidade em transação única (UC06).
5. Calcula subtotal, desconto do canal e total no servidor.
6. Grava AGUARDANDO_PAGAMENTO, canal, preços históricos e prazo; retorna 201.
7. Ator envia a solicitação de pagamento (UC15).
8. API autoriza acesso e bloqueia o pedido para verificar estado e prazo.
9. Se houver crédito, valida o saldo do titular (UC07).
10. Define número da tentativa e chama o gateway com valor externo (UC14).
11. Recebe e valida APROVADO, transação, valor e moeda.
12. Persiste tentativa e payloads; debita crédito escolhido; registra ganhos elegíveis.
13. Converte cada bloco completo de 100 pontos em R$ 1,00 de crédito.
14. Altera pedido para PAGO, registra auditoria e retorna 200 com os detalhes.
15. Operadores podem seguir com preparo e entrega (UC13), fora desta feature de pagamento.

### Pós-condições

**Sucesso:** pedido PAGO; estoque baixado uma única vez; aprovação e tentativas
rastreáveis; crédito consumido uma única vez e ganhos elegíveis registrados.

**Criação malsucedida:** nenhuma baixa parcial ou pedido incompleto confirmado.

**Pagamento recusado:** tentativa registrada, sem ganhos nem débito de crédito;
pedido ainda aguardando pagamento enquanto houver prazo. O estoque permanece
baixado até cancelamento antes do preparo ou expiração.

**Falha técnica externa:** nenhum pagamento/fidelidade confirmado localmente;
repetição mantém a chave da tentativa cujo resultado externo é desconhecido.
Uma falha local não garante que o provedor não tenha concluído sua operação.

### Exceções da feature

| Situação | Resposta e comportamento |
|---|---|
| JWT ausente/inválido | 401; não executa operação protegida. |
| Cliente paga pedido alheio ou Cozinha tenta pagar | 403; gateway não é chamado. |
| JSON/UUID/enum inválido | 400. |
| Campos obrigatórios ou valores inválidos | 422. |
| Unidade/produto inexistente ou unidade inativa | 404 na criação. |
| Produto inativo/fora do cardápio | 409; criação sem baixas parciais. |
| Estoque insuficiente | 409; rollback de todas as baixas. Ausência de registro de estoque retorna 404. |
| Pagamento negado | 200 com RECUSADO; registra tentativa e permite outra dentro do prazo. |
| Gateway falha ou retorna dados inconsistentes | 502; não confirma pagamento/fidelidade localmente. |
| Crédito insuficiente, acima do total ou sem consentimento/conta ativa | 409. |
| Pedido já pago, cancelado ou vencido | 409; não autoriza cobrança. |
| Duas aprovações simultâneas | Apenas uma pode ser concluída; a outra é bloqueada antes de nova cobrança. |
| Prazo de pagamento vencido | Bloqueia pagamento e agenda cancelamento com reposição. |
| Cancelamento pago | UC16 executa UC09 e trata estoque e fidelidade conforme a etapa. |

### Regras de negócio

| Regra | Definição |
|---|---|
| RN01 — Multicanalidade | `canalPedido` obrigatório: APP, WEB, TOTEM, BALCAO, PICKUP. Persistido e filtrável em `GET /pedidos?canalPedido=TOTEM`. |
| RN02 — Cardápio por unidade | Estoque ou catálogo geral não bastam: exige produto ativo e vínculo local habilitado. |
| RN03 — Valores | BigDecimal; preço vem do servidor e é gravado no item. Moeda BRL, sem conversão cambial. |
| RN04 — Promoção | Desconto de 10% para APP/WEB; duas casas e HALF_UP. |
| RN05 — Estoque | Bloqueio da unidade e transação única na criação. Pagamento não faz outra baixa. |
| RN06 — Acesso | CLIENTE paga somente pedido próprio. ATENDENTE/GERENTE/ADMIN possuem acesso operacional; COZINHA não paga. |
| RN07 — Tentativas | Número sequencial; recusa permite novo número; aprovação impede nova cobrança. |
| RN08 — Idempotência do pagamento | Gateway usa `pedidoId:numeroTentativa`. Repetição do mesmo payload/chave recupera a transação; alteração do payload com a mesma chave é rejeitada pelo mock. |
| RN09 — Limite da idempotência | Falha técnica sem registro local reutiliza o número. `POST /pedidos` não é idempotente; repeti-lo pode criar outro pedido. O cliente não envia header de idempotência à rota Raízes. |
| RN10 — Fidelidade | Um ponto por R$ 1,00 da parte externa aprovada, acumulando centavos. Exige consentimento e conta ativa no pagamento. Cada 100 pontos gera R$ 1,00; mantém o restante. |
| RN11 — Uso de crédito | Apenas debita em aprovação; pode pagar parte ou todo o pedido; não gera pontos sobre a própria parcela. |
| RN12 — Método mock | MOCK/PIX/CARTAO são simulações. `aprovadoMock` seleciona cenário, mas o backend usa o status recebido do provedor. `formaPagamento` da criação não é persistido nem confrontado com `metodo`. |
| RN13 — Prazo | Padrão 15 minutos; agendamento a cada 60 segundos, até 100 pedidos por execução; configurável. Pagamento verifica prazo mesmo antes do agendamento. |
| RN14 — Cancelamento | Antes do preparo repõe estoque; depois registra perda; pagamento aprovado é estornado, crédito usado devolvido e ganhos revertidos. Estados ENTREGUE/CANCELADO são finais. |
| RN15 — Idempotência do estorno | Chave `pagamentoId:estorno`; repetição não deve duplicar devolução enquanto o mock preservar seu histórico. |
| RN16 — Limitação do gateway | Histórico em memória. Reiniciar apenas o mock perde transações e pode impedir estornos. Tudo é simulado, sem dinheiro real. |

### Critérios de aceitação

1. Criação válida registra canal, preços e prazo, baixando somente o estoque da unidade.
2. Falha em qualquer item desfaz a criação inteira e as baixas parciais.
3. Aprovação registra payloads e altera o pedido para PAGO.
4. Recusa mantém histórico e permite nova tentativa sem consumo de crédito ou ganho de pontos.
5. Solicitações concorrentes não produzem duas aprovações do mesmo pedido.
6. Sem consentimento, comprar sem fidelidade continua possível; não há novos ganhos nem uso de crédito.
7. Pagamento fora do prazo é bloqueado; a expiração devolve estoque uma única vez.
8. Cancelamento trata estorno, estoque/perda e fidelidade de forma consistente.

## 6. Observações sobre o escopo do diagrama

Os 16 casos são uma visão funcional, não uma equivalência de uma elipse por endpoint.
UC11 agrupa várias operações administrativas; UC06, UC09 e UC14 representam
comportamentos internos/reutilizados. Rotas auxiliares de manutenção do próprio
cadastro e de consulta de produtos continuam existindo na API, sem exigir novas
elipses nesta versão.

As tabelas deixam explícitas as permissões adicionais para evitar desenhar
exclusividades que o backend não implementa. Se o trabalho exigir todas as
associações visíveis, acrescente também COZINHA → UC05 e os acessos próprios a UC03.

O pagamento aprovado é registrado em PROCESSAR_PAGAMENTO; a criação em CRIAR_PEDIDO;
transições operacionais em ATUALIZAR_STATUS_PEDIDO; cancelamento/expiração em
CANCELAR_PEDIDO. Expiração tem executor sistema, sem usuário humano.
