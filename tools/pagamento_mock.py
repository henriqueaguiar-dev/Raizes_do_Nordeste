#!/usr/bin/env python3
"""API de pagamento ficticia, local e sem integracao financeira real."""
import argparse
import json
import threading
import uuid
from decimal import Decimal
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

transacoes = {}
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
            if (self.headers.get('Idempotency-Key') != pedido_id
                    or dados['canalPedido'] not in ('APP', 'TOTEM', 'BALCAO', 'PICKUP', 'WEB')
                    or dados['metodo'] not in ('MOCK', 'PIX', 'CARTAO')
                    or dados['moeda'] != 'BRL' or type(dados['aprovadoMock']) is not bool
                    or not valor.is_finite() or valor < 0 or valor > Decimal('99999999.99')
                    or valor != valor.quantize(Decimal('0.01'))):
                raise ValueError('Solicitacao invalida')
            with bloqueio:
                if pedido_id in transacoes:
                    anterior, retorno = transacoes[pedido_id]
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
                    transacoes[pedido_id] = (dados, retorno)
                    status_http = 201
            self.responder(status_http, retorno)
        except (ValueError, KeyError, TypeError, ArithmeticError):
            self.responder(400, {'erro': 'SOLICITACAO_INVALIDA'})


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
