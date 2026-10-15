#!/usr/bin/env python3
"""API de pagamento ficticia, local e sem integracao financeira real."""
import argparse
import json
import threading
import uuid
from decimal import Decimal
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

transacoes = {}
estornos = {}
estornos_por_transacao = {}
bloqueio = threading.Lock()


class PagamentoMock(BaseHTTPRequestHandler):
    def responder(self, status, payload):
        corpo = json.dumps(payload, ensure_ascii=False).encode('utf-8')
        self.send_response(status)
        self.send_header('Content-Type', 'application/json; charset=utf-8')
        self.send_header('Content-Length', str(len(corpo)))
        self.end_headers()
        self.wfile.write(corpo)

    def do_POST(self):
        if self.path == '/estornos':
            self.estornar()
            return
        if self.path != '/pagamentos':
            self.responder(404, {'erro': 'ROTA_INEXISTENTE'})
            return
        try:
            tamanho = int(self.headers.get('Content-Length', '0'))
            if not 0 < tamanho <= 8192:
                raise ValueError('Tamanho invalido')
            dados = json.loads(self.rfile.read(tamanho), parse_float=Decimal)
            pedido_id = str(uuid.UUID(dados['pedidoId']))
            valor = Decimal(str(dados['valor']))
            tentativa = dados['numeroTentativa']
            chave = f'{pedido_id}:{tentativa}'
            if (self.headers.get('Idempotency-Key') != chave
                    or type(tentativa) is not int or tentativa < 1
                    or dados['canalPedido'] not in ('APP', 'TOTEM', 'BALCAO', 'PICKUP', 'WEB')
                    or dados['metodo'] not in ('MOCK', 'PIX', 'CARTAO')
                    or dados['moeda'] != 'BRL' or type(dados['aprovadoMock']) is not bool
                    or not valor.is_finite() or valor < 0 or valor > Decimal('99999999.99')
                    or valor != valor.quantize(Decimal('0.01'))):
                raise ValueError('Solicitacao invalida')
            with bloqueio:
                if chave in transacoes:
                    anterior, retorno = transacoes[chave]
                    if anterior != dados:
                        self.responder(409, {'erro': 'CHAVE_IDEMPOTENTE_COM_OUTRO_PAYLOAD'})
                        return
                    status_http = 200
                else:
                    aprovado = dados['aprovadoMock']
                    retorno = {
                        'pedidoId': pedido_id,
                        'codigoTransacao': 'MOCK-' + str(uuid.uuid4()),
                        'status': 'APROVADO' if aprovado else 'RECUSADO',
                        'valor': float(valor),
                        'moeda': 'BRL',
                        'mensagem': 'Pagamento mock aprovado.' if aprovado else 'Pagamento mock recusado.'
                    }
                    transacoes[chave] = (dados, retorno)
                    status_http = 201
            self.responder(status_http, retorno)
        except (ValueError, KeyError, TypeError, ArithmeticError):
            self.responder(400, {'erro': 'SOLICITACAO_INVALIDA'})

    def estornar(self):
        try:
            tamanho = int(self.headers.get('Content-Length', '0'))
            if not 0 < tamanho <= 8192:
                raise ValueError('Tamanho invalido')
            dados = json.loads(self.rfile.read(tamanho), parse_float=Decimal)
            pagamento_id = str(uuid.UUID(dados['pagamentoId']))
            pedido_id = str(uuid.UUID(dados['pedidoId']))
            chave = pagamento_id + ':estorno'
            valor = Decimal(str(dados['valor']))
            if (self.headers.get('Idempotency-Key') != chave or dados['moeda'] != 'BRL'
                    or not valor.is_finite() or valor < 0):
                raise ValueError('Estorno invalido')
            with bloqueio:
                if chave in estornos:
                    anterior, retorno = estornos[chave]
                    if anterior != dados:
                        self.responder(409, {'erro': 'ESTORNO_DIVERGENTE'})
                        return
                    status = 200
                else:
                    original = next((r for _, r in transacoes.values()
                                     if r['codigoTransacao'] == dados['codigoTransacao']), None)
                    if (original is None or original['status'] != 'APROVADO'
                            or original['pedidoId'] != pedido_id
                            or Decimal(str(original['valor'])) != valor
                            or dados['codigoTransacao'] in estornos_por_transacao):
                        self.responder(409, {'erro': 'PAGAMENTO_NAO_ESTORNAVEL'})
                        return
                    retorno = dict(pagamentoId=pagamento_id, codigoEstorno='ESTORNO-' + str(uuid.uuid4()),
                                   status='ESTORNADO', valor=float(valor), moeda='BRL', mensagem='Estorno mock aprovado.')
                    estornos[chave] = (dados, retorno)
                    estornos_por_transacao[dados['codigoTransacao']] = chave
                    status = 201
            self.responder(status, retorno)
        except (ValueError, KeyError, TypeError, ArithmeticError):
            self.responder(400, {'erro': 'SOLICITACAO_ESTORNO_INVALIDA'})


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--port', type=int, default=9090)
    args = parser.parse_args()
    servidor = ThreadingHTTPServer(('127.0.0.1', args.port), PagamentoMock)
    print(f'Mock local: http://127.0.0.1:{args.port}/pagamentos (Ctrl+C para encerrar)', flush=True)
    try:
        servidor.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        servidor.server_close()
