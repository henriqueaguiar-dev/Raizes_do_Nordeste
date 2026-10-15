# Diagrama de Casos de Uso — Raízes API

## Escopo

Sistema de atendimento multicanal de uma rede de lanchonetes. Os canais APP, WEB,
TOTEM, BALCAO e PICKUP são registrados no domínio do pedido. App/Web/Totem são
interfaces do ator Cliente; o backend não implementa essas interfaces gráficas.

![Diagrama de casos de uso](casos-de-uso.svg)

Fonte editável em PlantUML: [casos-de-uso.puml](casos-de-uso.puml).

As associações do diagrama destacam responsabilidades principais. A tabela abaixo
explicita as permissões atuais, inclusive operações compartilhadas entre perfis.

## Atores e responsabilidades

| Ator | Responsabilidades e permissões atuais |
|---|---|
| Cliente (App/Web/Totem) | Cadastrar-se, autenticar-se, consultar unidades/cardápio, realizar pedidos, consultar seus pedidos, solicitar/consultar seus pagamentos e consultar sua fidelidade. |
| Atendente (Balcão) | Autenticar-se, realizar e consultar pedidos, solicitar/consultar pagamentos, atualizar status e cancelar pedidos. |
| Cozinha | Autenticar-se, consultar pedidos, atualizar status operacional e cancelar pedidos. Não pode processar ou consultar pagamentos. |
| Gerente / Administrador | Gerenciar produtos, unidades, cardápio e estoque; consultar/realizar pedidos, processar pagamentos, atualizar status e cancelar. Somente ADMIN cadastra e mantém usuários internos e consulta saldo de fidelidade de outra conta. |
| Sistemas Externos de Pagamento (Gateway) | Receber solicitações HTTP de pagamento/estorno e retornar status e payload. Nesta implementação é uma API mock local separada, sem movimentação financeira real. |

O cadastro público sempre gera CLIENTE. Os perfis internos são criados por ADMIN.
A consulta de unidades e cardápio é pública; autenticação é pré-condição para criar
pedidos e solicitar pagamentos. Atualmente os perfis operacionais consultam pedidos
sem restrição por unidade e compartilham as permissões de alteração de status.
O diagrama não atribui exclusividade de uma etapa à Cozinha ou ao Atendente.

No balcão, a implementação atual usa o titular do token como `clienteId` do pedido.
A associação de um pedido feito por atendente ao cadastro de outro cliente não está
implementada; não é pressuposta nos fluxos abaixo.

## Relações UML

- **Associação:** comunicação entre ator e caso de uso.
- **«include»:** comportamento obrigatório reutilizado. Realizar Pedido inclui
  Validar/baixar estoque; Solicitar Pagamento inclui Integrar pagamento mock.
- **«extend»:** comportamento condicional. Usar crédito de fidelidade estende
  Solicitar Pagamento quando o usuário informa um valor positivo; Solicitar Estorno
  estende Cancelar Pedido quando existe pagamento aprovado.
- Realizar Pedido e Solicitar Pagamento são casos distintos: criar um pedido não
  executa automaticamente seu pagamento.
- A expiração é um disparo temporal interno do sistema, sem um ator humano adicional.

# Feature F01 — Realizar Pedido + Solicitar Pagamento

## Objetivo e identificação

Permitir que um usuário crie um pedido em uma unidade, com rastreabilidade do canal,
e obtenha a confirmação ou recusa do pagamento simulado, mantendo consistência do
estoque, dos valores e da fidelidade.

| Item | Definição |
|---|---|
| Casos de uso | UC04 — Realizar Pedido; UC05 — Solicitar Pagamento |
| Atores iniciadores | Cliente ou Atendente; Gerente/Administrador também possuem permissão operacional. |
| Ator de apoio | Sistemas Externos de Pagamento (Gateway mock). |
| Gatilho | Usuário confirma produtos, quantidades e unidade e, depois, solicita o pagamento. |
| Canal | APP, TOTEM, BALCAO, PICKUP ou WEB, obrigatório e gravado no pedido. |

## Pré-condições

1. Usuário cadastrado e autenticado por JWT válido para as operações protegidas.
2. Unidade ativa; produtos ativos e habilitados no cardápio dessa unidade.
3. Estoque local suficiente para todas as quantidades solicitadas.
4. Para pagar: pedido em AGUARDANDO_PAGAMENTO, dentro do prazo e sem aprovação anterior.
5. Serviço HTTP mock disponível para concluir o pagamento.
6. Para usar crédito: usuário ativo, consentimento vigente e saldo suficiente.
   Consentimento não é exigido para comprar sem usar fidelidade.

## Fluxo principal

### UC04 — Realizar Pedido

1. O ator consulta `GET /unidades` e escolhe uma unidade ativa.
2. Consulta `GET /unidades/{unidadeId}/cardapio` e escolhe produtos disponíveis.
3. Envia `POST /pedidos` com `unidadeId`, `canalPedido`, `formaPagamento` e itens.
4. A API identifica o titular pelo JWT e valida campos, unidade e produtos.
5. A API verifica habilitação no cardápio e quantidade em estoque na unidade.
6. Calcula preços, subtotais, desconto por canal e total, sem aceitar preço do cliente.
7. Baixa o estoque dentro de transação protegida por bloqueio da unidade.
8. Registra pedido em AGUARDANDO_PAGAMENTO, preços dos itens, canal e prazo `expiraEm`.
9. Registra auditoria de criação e retorna HTTP 201 com o pedido.

### UC05 — Solicitar Pagamento

10. O ator envia `POST /pagamentos/pedidos/{pedidoId}` com `metodo`, `aprovadoMock`
    e, opcionalmente, `valorFidelidade`.
11. A API bloqueia o pedido e verifica titularidade/perfil, estado, prazo e ausência
    de pagamento aprovado. Valida também o crédito de fidelidade solicitado.
12. Atribui o número sequencial da tentativa e envia ao gateway mock o pedido,
    canal, método, moeda BRL e valor externo, com chave idempotente.
13. O gateway retorna o resultado; no fluxo de sucesso, APROVADO e código de transação.
14. A API valida o retorno e registra tentativa, payload enviado, payload recebido
    e status HTTP do gateway.
15. Debita o crédito utilizado, registra ganhos elegíveis e converte blocos completos
    de 100 pontos em crédito. Essas alterações e o pagamento usam a mesma transação.
16. Altera o pedido para PAGO, registra auditoria e retorna HTTP 200 com o resultado.
17. A Cozinha/operador pode prosseguir com EM_PREPARO → PRONTO → ENTREGUE, pelo
    `PATCH /pedidos/{id}/status`, como caso de uso operacional separado.

## Pós-condições

### Sucesso

- Pedido em PAGO, com itens, valores históricos e canal identificados.
- Estoque da unidade baixado uma única vez na criação, não nas tentativas de pagamento.
- Uma única aprovação, com todas as tentativas anteriores preservadas.
- Integração rastreável por pedido, número da tentativa, código e payloads do gateway.
- Crédito utilizado debitado apenas uma vez; ganhos registrados somente se elegíveis.

### Falha

- Falha ao criar: nenhuma baixa parcial nem pedido incompleto permanece gravado.
- Recusa: tentativa registrada; pedido continua aguardando pagamento dentro do prazo.
- Falha técnica do gateway: nenhum pagamento ou efeito de fidelidade é confirmado
  localmente; a reserva de estoque do pedido permanece até cancelamento/expiração.
- O estoque não volta automaticamente em toda falha de pagamento: ele é recomposto
  por cancelamento antes do preparo ou por expiração.

## Exceções e fluxos alternativos

| Código | Situação | Comportamento |
|---|---|---|
| E01 | JWT ausente/inválido | HTTP 401; operação protegida não executada. |
| E02 | Cliente tenta pagar pedido de outra conta; Cozinha tenta pagar | HTTP 403; gateway não é chamado. |
| E03 | JSON, UUID ou enum inválido | HTTP 400. Campos obrigatórios ausentes, quantidades não positivas ou formato monetário inválido: HTTP 422. |
| E04 | Unidade inexistente/inativa ou produto inexistente | HTTP 404; criação sem efeitos parciais. |
| E05 | Produto inativo ou fora do cardápio da unidade | HTTP 409; nenhuma baixa parcial permanece. |
| E06 | Estoque menor que o solicitado | HTTP 409 ESTOQUE_INSUFICIENTE; transação de criação desfeita. Ausência do registro de estoque retorna 404. |
| E07 | Gateway recusa o pagamento | Rota Raízes retorna HTTP 200 com status RECUSADO; não gera pontos nem debita crédito. Nova tentativa pode usar outro método enquanto houver prazo. |
| E08 | Crédito insuficiente, acima do total ou sem consentimento/usuário ativo | HTTP 409; não registra pagamento nem consome saldo. |
| E09 | Pedido já pago, cancelado ou fora do prazo | HTTP 409; bloqueia cobrança. |
| E10 | Gateway indisponível, timeout, erro HTTP ou retorno inconsistente | HTTP 502; transação local de pagamento desfeita. Repetição deve manter o payload da tentativa de resultado externo desconhecido. |
| E11 | Dois pagamentos simultâneos | Bloqueio do pedido serializa as operações; após uma aprovação, a outra é rejeitada sem nova chamada ao gateway. |
| E12 | Pagamento não solicitado/concluído no prazo | Agendamento cancela o pedido, registra motivo de expiração e recompõe estoque. |
| E13 | Cancelamento antes do preparo | Repõe estoque; se pago, estorna no mock, devolve crédito usado e reverte ganhos da compra. |
| E14 | Cancelamento em EM_PREPARO ou PRONTO | Registra perda sem repor estoque; se pago, estorna pagamento e ajusta fidelidade. |
| E15 | Estorno falha | HTTP 502; cancelamento e efeitos locais são desfeitos. Repetição usa a mesma chave de estorno. |

## Regras de negócio

**RN01 — Multicanalidade.** `canalPedido` obrigatório: APP, TOTEM, BALCAO, PICKUP, WEB.
É persistido e pode ser consultado por `GET /pedidos?canalPedido=TOTEM`.

**RN02 — Cardápio local.** Existir no catálogo geral ou ter estoque não habilita a
venda. O produto precisa de vínculo ativo com o cardápio da unidade. A disponibilidade
exibida é informativa e é revalidada na criação.

**RN03 — Valores.** Cálculo no servidor com BigDecimal. APP e WEB recebem 10% de
desconto no subtotal, arredondado para duas casas com HALF_UP. A moeda é BRL;
não existe conversão cambial.

**RN04 — Estoque.** A criação baixa os itens em transação única, sob bloqueio da
unidade. Falhar em qualquer item desfaz todas as baixas. Retentar pagamento não
repete a baixa do estoque.

**RN05 — Autorização.** CLIENTE paga apenas pedido próprio. ADMIN, GERENTE e
ATENDENTE têm permissão operacional de pagamento. COZINHA não tem essa permissão.

**RN06 — Tentativas.** Cada tentativa confirmada recebe numeração crescente. Uma
recusa permite nova tentativa; uma aprovação impede nova cobrança. Consultas:
`GET /pagamentos/pedidos/{pedidoId}` (mais recente) e
`GET /pagamentos/pedidos/{pedidoId}/tentativas` (histórico).

**RN07 — Idempotência.** Chave do pagamento externo: `pedidoId:numeroTentativa`.
Repetir a mesma tentativa devolve a mesma transação no mock; mudar seu payload
com a mesma chave gera conflito. Falha técnica sem registro local reutiliza o número
na repetição. Recusa confirmada gera novo número. A API Raízes não exige um header
de idempotência do cliente e a criação de pedidos não é idempotente: repetir
`POST /pedidos` pode criar outro pedido.

**RN08 — Fidelidade.** Um ponto por R$ 1,00 da parcela externa aprovada, acumulando
centavos elegíveis. Exige usuário ativo e consentimento no pagamento. Cada 100
pontos vira automaticamente R$ 1,00; o restante permanece. A parcela paga com
fidelidade não gera pontos. Crédito pode pagar parte ou todo o total; mesmo com
valor externo zero, o fluxo atual continua solicitando resultado ao mock.

**RN09 — Forma de pagamento.** `formaPagamento` é obrigatório na criação, mas não
é persistido nem confrontado com `metodo`; o método efetivo é o do pagamento.
`MOCK`, `PIX` e `CARTAO` são todos simulados. `aprovadoMock` seleciona o cenário
no gateway; a API decide a partir do status recebido, não diretamente desse campo.

**RN10 — Prazo.** Pagamento permitido por 15 minutos por padrão. Verificação de
expiração a cada 60 segundos, até 100 pedidos por execução, com parâmetros
configuráveis. O pagamento verifica o prazo mesmo antes da execução do agendamento.

**RN11 — Cancelamento.** Estados finais: ENTREGUE e CANCELADO. Apenas perfis
operacionais cancelam pela rota atual. Antes do preparo, devolve estoque; depois,
registra perda. Estorno externo usa `pagamentoId:estorno`. Reverte pontos gerados
e devolve crédito utilizado independentemente do consentimento atual. Se o crédito
revertido já foi gasto, o saldo pode ficar negativo para compensação com ganhos futuros.

**RN12 — Limites do mock.** Histórico do gateway é em memória. Reiniciar somente
esse processo perde a idempotência e as transações necessárias ao estorno. Em caso
de timeout, a operação externa pode ter concluído; repetir a mesma chave permite
reconciliar somente enquanto o histórico do mock existir. Não há pagamento real.

## Critérios de aceitação

1. Pedido válido cria registro com canal obrigatório, valores calculados e estoque local atualizado.
2. Estoque insuficiente impede criação sem deixar baixas parciais.
3. Aprovação registra payloads e muda pedido para PAGO.
4. Recusa permite nova tentativa e preserva o histórico sem gerar benefícios.
5. Solicitações simultâneas não produzem duas aprovações do mesmo pedido.
6. Sem consentimento não há novos pontos nem uso de crédito de fidelidade.
7. Crédito só é debitado em aprovação e não gera novos pontos sobre sua própria parcela.
8. Prazo vencido impede cobrança e resulta em expiração com recomposição de estoque.
9. Cancelamento trata estoque/perda, estorno e fidelidade conforme a etapa.

## Referências de implementação

- Controladores: `api/controlador/PedidoControlador.java`, `PagamentoControlador.java` e `CardapioControlador.java`.
- Regras: `aplicacao/servico/PedidoServico.java`, `PagamentoServico.java`, `FidelidadeServico.java` e `CancelamentoPedidoServico.java`.
- Expiração: `aplicacao/servico/ExpiracaoPedidosServico.java`.
- Integração: `infraestrutura/pagamento/ProvedorPagamentoMockHttp.java` e `tools/pagamento_mock.py`.

Os caminhos Java acima são relativos a `src/main/java/com/raizes/raizes_api/`.
