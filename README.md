# Raízes API

**API Back-end** desenvolvida em **Java com Spring Boot** para a rede de lanchonetes **Raízes do Nordeste**.

O sistema permite o cadastro e a autenticação de usuários, controle de produtos, unidades, estoque, pedidos, pagamento mock, auditoria, segurança com JWT/BCrypt, controle de acesso por perfis, promoção por canal e programa de fidelidade.

## Tecnologias Utilizadas
- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server / JWT
- BCrypt
- H2 Database
- Swagger / OpenAPI
- Postman

## Funcionalidades
- Cadastro público de clientes
- Cadastro protegido de usuários internos
- Login com BCrypt
- Autenticação com JWT
- Autorização por perfis
- Cadastro e consulta de produtos
- Cardápio por unidade, com habilitação de produtos e disponibilidade baseada no estoque local
- Cadastro e consulta de unidades
- Controle de estoque por unidade
- Criação de pedidos com `canalPedido`
- Desconto de 10% para pedidos via APP e WEB
- Pagamento mock (aprovado ou recusado)
- Atualização de status do pedido
- Auditoria da criação de pedidos, alteração de status e processamento de pagamentos
- Tratamento padronizado de erros
- Documentação via Swagger/OpenAPI

## Perfis de Usuário
O sistema possui os seguintes perfis:
- CLIENTE
- ATENDENTE
- COZINHA
- GERENTE
- ADMIN

O cadastro público cria sempre usuários com o perfil **CLIENTE**.  
Perfis internos, como **ADMIN, GERENTE, COZINHA e ATENDENTE**, devem ser criados obrigatoriamente por um usuário **ADMIN**.

## Requisitos para Executar
Antes de executar, instale:
- Java 21
- Maven (ou use o Maven Wrapper do projeto)
- Postman, Insomnia ou Swagger para testar a API

## Variáveis de Ambiente
Antes de iniciar a aplicação, configure as variáveis de ambiente:

**No Linux/macOS:**
```bash
export ADMIN_INICIAL_NOME="Administrador Raizes"
export ADMIN_INICIAL_EMAIL="admin@raizes.com"
export ADMIN_INICIAL_SENHA="Admin@123"
export JWT_SECRET="chave-super-secreta-com-mais-de-32-caracteres"
```

**No Windows (PowerShell):**
```powershell
$env:ADMIN_INICIAL_NOME="Administrador Raizes"
$env:ADMIN_INICIAL_EMAIL="admin@raizes.com"
$env:ADMIN_INICIAL_SENHA="Admin@123"
$env:JWT_SECRET="chave-super-secreta-com-mais-de-32-caracteres"
```
*Essas variáveis são usadas para criar o administrador inicial e assinar os tokens JWT.*

### Usuário Administrador Inicial
Ao iniciar a aplicação, o sistema cria automaticamente um usuário administrador caso ele ainda não exista, utilizando os dados definidos pelas variáveis de ambiente:
- **Email:** admin@raizes.com
- **Senha:** Admin@123
- **Perfil:** ADMIN

*Use credenciais próprias nas variáveis de ambiente. O projeto ainda não oferece endpoint para troca de senha.*

## Como Iniciar o Projeto
Na raiz do projeto, execute:
```bash
./mvnw spring-boot:run
```
**No Windows:**
```cmd
mvnw spring-boot:run
```
A API ficará disponível em: [http://localhost:8080](http://localhost:8080)

### H2 Console
O projeto usa H2 em memória para desenvolvimento local.
- **Acesse:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL:** `jdbc:h2:mem:raizesdb`
- **User Name:** `sa`
- **Password:** *vazio*

### Swagger
A documentação da API está disponível em: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

**Para testar endpoints protegidos no Swagger:**
1. Faça login em `/auth/login`.
2. Copie o `accessToken`.
3. Clique em **Authorize**.
4. Informe somente o token JWT, sem o prefixo `Bearer` (o Swagger adiciona esse prefixo).

---

## Autenticação

### Login
`POST /auth/login`

**Request:**
```json
{
  "email": "admin@raizes.com",
  "senha": "Admin@123"
}
```

**Response:**
```json
{
  "usuarioId": "uuid",
  "nome": "Administrador Raizes",
  "email": "admin@raizes.com",
  "perfil": "ADMIN",
  "accessToken": "jwt...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```
*Para acessar endpoints protegidos, envie o token no header:*  
`Authorization: Bearer TOKEN`

---

## Endpoints da API

### Usuários

#### Cadastrar cliente
`POST /usuarios`  
*Endpoint público. Sempre cria usuário com perfil CLIENTE.*

**Request:**
```json
{
  "nome": "Maria Cliente",
  "email": "maria@exemplo.com",
  "senha": "Senha@123",
  "consentimentoLgpd": true
}
```

**Response:**
```json
{
  "id": "uuid",
  "nome": "Maria Cliente",
  "email": "maria@exemplo.com",
  "perfil": "CLIENTE",
  "ativo": true,
  "consentimentoLgpd": true
}
```

#### Consultar e manter usuários
- `GET /usuarios`: lista clientes ativos e inativos; somente ADMIN.
- `GET /usuarios/internos`: lista ATENDENTE, COZINHA e GERENTE; somente ADMIN.
- `PUT /usuarios/{id}` e `DELETE /usuarios/{id}`: CLIENTE titular ou ADMIN.
- `PUT /usuarios/internos/{id}` e `DELETE /usuarios/internos/{id}`: somente ADMIN.

PUT recebe `nome`, `email` e `consentimentoLgpd`; preserva senha, perfil, unidade e status ativo.
DELETE marca o usuário como inativo, sem apagar o registro. A desativação impede novos logins;
um JWT já emitido continua válido até expirar. Não existe endpoint de troca de senha neste projeto.

**Exemplo de request para atualizar usuário (cliente ou interno):**
```json
{
  "nome": "Maria Atualizada",
  "email": "maria.atualizada@exemplo.com",
  "consentimentoLgpd": true
}
```
Os três campos são obrigatórios; o PUT não é uma atualização parcial.

#### Cadastrar usuário interno
`POST /usuarios/internos`  
*Acesso restrito: ADMIN*

**Request:**
```json
{
  "nome": "Joao Gerente",
  "email": "gerente@raizes.com",
  "senha": "Senha@123",
  "perfil": "GERENTE",
  "consentimentoLgpd": true,
  "unidadeId": "uuid-de-uma-unidade-existente"
}
```
*Perfis permitidos:* ADMIN, GERENTE, COZINHA, ATENDENTE.
Crie primeiro uma unidade e informe seu ID no campo obrigatório `unidadeId`.

---

### Produtos

#### Criar produto
`POST /produtos`  
*Acesso: ADMIN ou GERENTE*

**Request:**
```json
{
  "nome": "X-Burger Nordestino",
  "descricao": "Hambúrguer artesanal com queijo coalho e molho da casa.",
  "preco": 29.90
}
```

#### Listar produtos
`GET /produtos`  
*Acesso: público*

#### Buscar produto por ID
`GET /produtos/{id}`  
*Acesso: público*

#### Atualizar produto
`PUT /produtos/{id}`  
*Acesso: ADMIN ou GERENTE*

**Request:**
```json
{
  "nome": "X-Burger Nordestino",
  "descricao": "Hambúrguer artesanal com queijo coalho e molho da casa.",
  "preco": 32.90,
  "ativo": true
}
```
Todos os campos são obrigatórios. O preço deve ser maior que zero, ter no máximo
oito dígitos inteiros e duas casas decimais. `ativo: false` desativa o produto;
`ativo: true` permite reativá-lo.

#### Desativar produto
`DELETE /produtos/{id}`  
*Acesso: ADMIN ou GERENTE (O produto não é removido fisicamente do banco, apenas marcado como inativo).*

---

### Unidades

#### Criar unidade
`POST /unidades`  
*Acesso: ADMIN ou GERENTE*

**Request:**
```json
{
  "nome": "Unidade Centro",
  "endereco": "Rua Principal, 100"
}
```

#### Listar unidades
`GET /unidades`  
*Acesso: público*

#### Buscar unidade por ID
`GET /unidades/{id}`  
*Acesso: público*

#### Atualizar unidade
`PUT /unidades/{id}`  
*Acesso: ADMIN ou GERENTE*

**Request:**
```json
{
  "nome": "Unidade Centro",
  "endereco": "Rua Principal, 200",
  "ativa": true
}
```
Todos os campos são obrigatórios. `ativa: false` desativa a unidade;
`ativa: true` permite reativá-la. Unidades inativas não aceitam novos pedidos.

#### Desativar unidade
`DELETE /unidades/{id}`  
*Acesso: ADMIN ou GERENTE*

---

### Cardápio por unidade

O cadastro `/produtos` é o catálogo geral da rede. Cada unidade possui seu próprio
cardápio, configurado explicitamente; cadastrar um produto ou registrar estoque
não o inclui automaticamente no cardápio. O preço utilizado continua sendo o preço
geral cadastrado em `/produtos`.

#### Incluir, habilitar ou desabilitar um produto
`PUT /unidades/{unidadeId}/cardapio/produtos/{produtoId}`  
*Acesso: ADMIN ou GERENTE*

Crie a unidade e o produto primeiro. O PUT cria o vínculo quando ele não existe
ou atualiza o vínculo existente, sem duplicá-lo. O campo `ativo` é obrigatório.

**Request:**
```json
{
  "ativo": true
}
```

**Response (200):**
```json
{
  "unidadeId": "uuid-da-unidade",
  "produtoId": "uuid-do-produto",
  "ativo": true
}
```

Envie `ativo: false` para retirar o produto apenas do cardápio dessa unidade;
envie `ativo: true` para reativá-lo. A configuração pode ser preparada mesmo
quando a unidade ou o produto estiverem inativos, mas isso não libera vendas.
Unidade ou produto inexistente retorna 404.

#### Consultar cardápio
`GET /unidades/{unidadeId}/cardapio`  
*Acesso: público, para APP, TOTEM, BALCÃO, PICKUP e WEB*

**Response (200):**
```json
[
  {
    "produtoId": "uuid-do-produto",
    "nome": "X-Burger Nordestino",
    "descricao": "Hambúrguer artesanal com queijo coalho e molho da casa.",
    "preco": 29.90,
    "disponivel": true
  }
]
```

A consulta retorna somente produtos ativos no catálogo geral e habilitados no
cardápio da unidade, ordenados por nome. Produtos sem registro de estoque ou com
saldo zero permanecem visíveis com `disponivel: false`. A disponibilidade usa
somente o estoque da unidade consultada e muda conforme entradas, saídas e pedidos.
Um cardápio sem produtos retorna `[]`; unidade inexistente ou inativa retorna 404.

Para começar a vender: cadastre unidade e produto, habilite o vínculo no cardápio
e registre entrada de estoque naquela unidade. Ao criar o pedido, a API verifica
novamente a habilitação e o saldo local; produto fora do cardápio retorna 409,
mesmo que tenha estoque. Uma falha desfaz todas as baixas do pedido.

As alterações do cardápio usam o mesmo bloqueio por unidade das vendas, garantindo
que habilitação e baixa de estoque sejam verificadas de forma consistente.

---

### Estoques

#### Listar todo o estoque
`GET /estoques/total` — ADMIN ou GERENTE.

As movimentações e a criação de pedidos usam transações e bloqueiam a unidade durante
a alteração do estoque, evitando baixas perdidas e a criação simultânea de registros duplicados.
Um pedido com item inválido desfaz todas as baixas da mesma transação.

#### Registrar entrada de estoque
`POST /estoques/entradas`  
*Acesso: ADMIN ou GERENTE*

**Request:**
```json
{
  "unidadeId": "uuid-da-unidade",
  "produtoId": "uuid-do-produto",
  "quantidade": 20
}
```

#### Registrar saída de estoque
`POST /estoques/saidas`  
*Acesso: ADMIN ou GERENTE*

**Request:**
```json
{
  "unidadeId": "uuid-da-unidade",
  "produtoId": "uuid-do-produto",
  "quantidade": 2
}
```

#### Listar estoque por unidade
`GET /estoques/unidades/{unidadeId}`  
*Acesso: ADMIN ou GERENTE*

#### Consultar estoque por unidade e produto
`GET /estoques/unidades/{unidadeId}/produtos/{produtoId}`  
*Acesso: ADMIN ou GERENTE*

---

### Pedidos

#### Criar pedido
`POST /pedidos`  
*Acesso: usuário autenticado (O cliente não envia `clienteId`, o sistema o identifica pelo token JWT).*

**Request:**
```json
{
  "unidadeId": "uuid-da-unidade",
  "canalPedido": "APP",
  "formaPagamento": "MOCK",
  "itens": [
    {
      "produtoId": "uuid-do-produto",
      "quantidade": 2
    }
  ]
}
```
*Canais aceitos:* APP, TOTEM, BALCAO, PICKUP, WEB.
*Pedidos feitos por **APP** ou **WEB** recebem desconto de 10%.*
Todos os itens devem estar ativos no catálogo geral, habilitados no cardápio da
unidade informada e ter estoque suficiente nessa unidade.

`formaPagamento` é obrigatório e aceita `MOCK`, `PIX` ou `CARTAO`. Atualmente esse
campo é validado, mas não é persistido nem utilizado no processamento do pedido.
O método efetivamente registrado é o campo `metodo` enviado ao endpoint de pagamento;
a aplicação não exige que ele seja igual à `formaPagamento` da criação do pedido.

**Response:**
```json
{
  "id": "uuid",
  "clienteId": "uuid",
  "unidadeId": "uuid",
  "canalPedido": "APP",
  "status": "AGUARDANDO_PAGAMENTO",
  "subtotal": 59.80,
  "valorDesconto": 5.98,
  "total": 53.82,
  "itens": [
    {
      "produtoId": "uuid",
      "quantidade": 2,
      "precoUnitario": 29.90,
      "subtotal": 59.80
    }
  ]
}
```

#### Listar pedidos
`GET /pedidos`  
*Acesso: usuário autenticado*  
*Filtros opcionais:* `GET /pedidos?canalPedido=APP&status=AGUARDANDO_PAGAMENTO`

**Regras:**
- **CLIENTE** vê apenas seus próprios pedidos.
- **ADMIN, GERENTE, COZINHA e ATENDENTE** podem consultar todos os pedidos, sem restrição por unidade, respeitando os filtros opcionais enviados.

#### Buscar pedido por ID
`GET /pedidos/{id}`  
*Acesso: usuário autenticado (O cliente só pode acessar os próprios pedidos).*

#### Atualizar status do pedido
`PATCH /pedidos/{id}/status`  
*Acesso: usuário autenticado com permissão operacional*

**Request:**
```json
{
  "status": "EM_PREPARO"
}
```
*Fluxo de status:* AGUARDANDO_PAGAMENTO → PAGO → EM_PREPARO → PRONTO → ENTREGUE.
Somente um pagamento aprovado muda o pedido para PAGO. Antes de ENTREGUE, o pedido pode
ser CANCELADO. ENTREGUE e CANCELADO são estados finais.
A alteração operacional exige ADMIN, GERENTE, COZINHA ou ATENDENTE.

---

### Pagamentos

#### Processar pagamento mock
`POST /pagamentos/pedidos/{pedidoId}`  
*Acesso: usuário autenticado*

O campo `metodo` aceita `MOCK`, `PIX` e `CARTAO`. Todos os métodos são simulados:
não há integração com banco, operadora de cartão ou provedor de PIX.
`aprovadoMock` é obrigatório e determina se o pagamento será aprovado ou recusado.

**Request aprovado:**
```json
{
  "metodo": "MOCK",
  "aprovadoMock": true
}
```
*Se o pagamento for aprovado, o status do pedido muda para **PAGO**.*

**Request recusado:**
```json
{
  "metodo": "MOCK",
  "aprovadoMock": false
}
```

O titular com perfil CLIENTE e usuários ADMIN, GERENTE ou ATENDENTE podem processar
ou consultar o pagamento. COZINHA e clientes de outras contas recebem 403.
Cada pedido admite um único pagamento registrado, inclusive se recusado; uma recusa
não permite nova tentativa nesse pedido. A auditoria registra o usuário que executou a ação.

#### Buscar pagamento por pedido
`GET /pagamentos/pedidos/{pedidoId}`  
*Acesso: usuário autenticado*

---

### Fidelidade

O programa usa `consentimentoLgpd` como adesão à fidelidade nesta versão.
Informe `true` no cadastro ou no `PUT /usuarios/{id}` para participar; `false`
retira a participação. O PUT continua exigindo nome, email e consentimento.
O consentimento é consultado no banco, sem necessidade de renovar o JWT.

**Acúmulo:** 1 ponto por R$ 1,00 da parte paga pelo mock em pagamentos aprovados,
com usuário ativo e consentimento. Centavos acumulam entre pagamentos elegíveis.
Pagamentos recusados e pagamentos feitos sem consentimento não geram pontos;
compras anteriores à adesão não recebem crédito retroativo.

**Conversão automática:** a cada **100 pontos, o usuário ganha R$ 1,00 de crédito**
para pagar pedidos na lanchonete. Todos os blocos completos são convertidos no
pagamento aprovado, mantendo o restante dos pontos. Exemplo: 250 pontos geram
R$ 2,00 de crédito e deixam 50 pontos. Cada conversão é registrada no histórico.
A parcela paga com crédito de fidelidade não gera novos pontos.

Retirar o consentimento bloqueia novos ganhos, conversões e uso do crédito.
Pontos, créditos e histórico anteriores são preservados e podem ser consultados.
Ao aderir novamente, os saldos voltam a poder ser usados. Não há atribuição
retroativa de pontos a pagamentos anteriores a esta implementação.

#### Consultar saldo
`GET /fidelidade/saldo` — próprio usuário autenticado.

```json
{
  "clienteId": "uuid",
  "pontosGanhos": 250,
  "pontosConvertidos": 200,
  "creditoConvertido": 2.00,
  "restante": 50,
  "saldoCredito": 2.00
}
```

Os campos são acumulados da conta: `pontosGanhos` mostra o total ganho,
`pontosConvertidos` mostra quantos já viraram crédito, `creditoConvertido` informa
esse valor em reais e `restante` mostra os pontos ainda não convertidos.
`saldoCredito` é o crédito disponível após pagamentos; pode ser menor que o total
convertido. O campo JSON anterior `pontos` foi substituído por `restante`.

`GET /fidelidade/saldo/{clienteId}` permite consulta pelo titular ou ADMIN.
Outros usuários recebem 403; conta inexistente retorna 404.

#### Pagar com crédito de fidelidade
`POST /pagamentos/pedidos/{pedidoId}`

```json
{
  "metodo": "MOCK",
  "aprovadoMock": true,
  "valorFidelidade": 1.00
}
```

`valorFidelidade` é opcional (padrão zero), não negativo e com até duas casas
decimais. Não pode ultrapassar o crédito disponível nem o total do pedido.
A resposta e o registro do pagamento distinguem:
- `valor`: total do pedido;
- `valorFidelidade`: parcela escolhida em crédito;
- `valorExterno`: restante pago pelo mock.

É possível pagar parte ou todo o pedido com crédito. O débito ocorre apenas
quando o pagamento é aprovado; recusa preserva o saldo. Todas as etapas ocorrem
na mesma transação, com bloqueio por usuário para impedir gasto simultâneo do
mesmo crédito. Usuário inativo ou sem consentimento não pode usar crédito.

#### Consultar conversões
`GET /fidelidade/resgates` — histórico do próprio titular, do mais recente para o
mais antigo. Retorna `[]` quando não houver conversões.

```json
[
  {
    "id": "uuid-da-conversao",
    "clienteId": "uuid-do-cliente",
    "pontosConsumidos": 200,
    "beneficio": "CREDITO_PAGAMENTO",
    "codigoResgate": "FID-uuid-da-conversao",
    "criadoEm": "2026-10-14T13:00:00-04:00",
    "valorCredito": 2.00
  }
]
```

`POST /fidelidade/resgates` permanece disponível, sem corpo, para converter
manualmente 100 pontos pendentes em R$ 1,00. No fluxo normal a conversão já é
automática e não precisa desse POST. Saldo inferior a 100 ou ausência de
consentimento retorna 409. A regra anterior de brinde foi substituída por crédito;
registros antigos de brinde não representam crédito monetário.

---

## Auditoria

A aplicação registra automaticamente estas ações no banco:
- `CRIAR_PEDIDO`: criação de pedido.
- `ATUALIZAR_STATUS_PEDIDO`: alteração operacional do status.
- `PROCESSAR_PAGAMENTO`: registro de pagamento aprovado ou recusado.

O registro identifica o usuário que executou a ação, o recurso afetado, os detalhes
e a data/hora. Cadastros, atualizações e desativações de usuários, produtos e unidades,
assim como movimentações manuais de estoque, não geram auditoria atualmente.
Não há endpoint público para consultar esses registros.

## Padrão de Erro
A API utiliza uma resposta padronizada para erros.

**Exemplo:**
```json
{
  "erro": "ESTOQUE_INSUFICIENTE",
  "mensagem": "Estoque insuficiente para o produto informado.",
  "detalhes": [],
  "dataHora": "2026-09-29T10:00:00-03:00",
  "caminho": "/pedidos"
}
```

### Principais Códigos de Erro
- **400** - Requisição inválida
- **401** - Usuário não autenticado
- **403** - Usuário sem permissão
- **404** - Recurso não encontrado
- **409** - Conflito de negócio ou de dados
- **422** - Campos que não atendem à validação
- **500** - Erro interno inesperado

JSON inválido, enums desconhecidos e UUIDs inválidos retornam 400. Erros de autenticação
e autorização também seguem o formato `ErroResposta`.

## Configuração e execução dos testes

`JWT_EXPIRATION_MINUTES` é opcional e vale 60 por padrão. O `expiresIn` do login
corresponde à duração configurada em segundos.

Com Java 21, execute a suíte local (H2 em memória):
```bash
./mvnw test
```
Os testes configuram suas próprias credenciais fictícias. O agente Mockito é configurado
no Maven para evitar depender de autoanexação da JVM.

A API e todos os testes usam H2 em memória. Nenhum serviço de banco externo é necessário.
Os dados se perdem ao encerrar a aplicação. O schema é gerado pelo Hibernate.

O desconto é arredondado para duas casas com HALF_UP. Preços também precisam ter no
máximo duas casas decimais. Pedidos cancelados não estornam estoque nem pagamentos;
os ganhos e débitos de fidelidade já registrados são preservados, mesmo em pedidos
cancelados; não há devolução automática de crédito nessa situação. Essas são as regras atuais do mock.

## Testes automáticos no GitHub (CI)

O workflow `.github/workflows/ci.yml` executa em cada push, em pull requests e
manualmente pela aba **Actions** do repositório. Usa Java 21, Maven Wrapper e H2.
Não precisa configurar banco externo nem credenciais da aplicação: os testes usam
configuração própria com dados fictícios.

A validação executa:
```bash
bash ./mvnw --batch-mode --no-transfer-progress clean test
```
Se algum teste falhar, a execução fica marcada como falha. Os relatórios de testes
são enviados mesmo quando a validação falha, se tiverem sido gerados, e ficam
disponíveis para download por 14 dias na página da execução.

Para ativar, envie o arquivo do workflow ao repositório e acompanhe a aba **Actions**.
Para exigir testes aprovados antes de integrar uma pull request, configure a regra
de proteção da branch `main` e selecione o check **Testes automaticos** como obrigatório
após a primeira execução.

Este workflow executa apenas os testes; não publica a aplicação.
