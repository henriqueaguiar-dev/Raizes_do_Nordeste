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
- Cadastro e consulta de unidades
- Controle de estoque por unidade
- Criação de pedidos com `canalPedido`
- Desconto de 10% para pedidos via APP e WEB
- Pagamento mock (aprovado ou recusado)
- Atualização de status do pedido
- Auditoria de ações sensíveis
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

*Nota: Em ambiente real, a senha inicial deve ser alterada após o primeiro acesso.*

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
4. Informe: `Bearer SEU_TOKEN`

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
  "consentimentoLgpd": true
}
```
*Perfis permitidos:* ADMIN, GERENTE, COZINHA, ATENDENTE.

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

#### Desativar unidade
`DELETE /unidades/{id}`  
*Acesso: ADMIN ou GERENTE*

---

### Estoques

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
*Canais aceitos:* APP, TOTEM, BALCÃO, PICKUP, WEB.  
*Pedidos feitos por **APP** ou **WEB** recebem desconto de 10%.*

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
- **ADMIN, GERENTE, COZINHA e ATENDENTE** podem consultar pedidos conforme a permissão configurada.

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
*Fluxo de status:* AGUARDANDO_PAGAMENTO $\rightarrow$ PAGO $\rightarrow$ EM_PREPARO $\rightarrow$ PRONTO $\rightarrow$ ENTREGUE $\rightarrow$ CANCELADO.

---

### Pagamentos

#### Processar pagamento mock
`POST /pagamentos/pedidos/{pedidoId}`  
*Acesso: usuário autenticado*

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

#### Buscar pagamento por pedido
`GET /pagamentos/pedidos/{pedidoId}`  
*Acesso: usuário autenticado*

---

### Fidelidade

#### Consultar saldo de pontos
`GET /fidelidade/saldo`  
*Acesso: usuário autenticado*

*Regra adotada: 1 ponto para cada R$ 1,00 pago em pedido aprovado.*

O saldo considera a soma dos pagamentos aprovados do usuário identificado pelo JWT.
Os centavos acumulam entre compras: R$ 10,50 + R$ 9,50 geram 20 pontos.
Uma soma de R$ 10,90 gera 10 pontos. Pagamentos recusados não geram pontos.
Envie `Authorization: Bearer SEU_TOKEN`; não é necessário informar o ID do cliente.

Para consultar uma conta específica, use `GET /fidelidade/saldo/{clienteId}`.
Somente o titular da conta e usuários com perfil **ADMIN** podem consultar esse saldo.
Outros usuários recebem **403 Forbidden**. Ambas as rotas exigem autenticação;
`GET /fidelidade/saldo` sempre retorna o saldo do próprio usuário autenticado.

**Exemplo de retorno:**
```json
{
  "clienteId": "uuid",
  "pontos": 90
}
```

---

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
