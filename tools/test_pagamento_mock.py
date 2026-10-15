import json
import threading
import unittest
import uuid
from http.server import ThreadingHTTPServer
from urllib.error import HTTPError
from urllib.request import Request, urlopen

import pagamento_mock


class PagamentoMockTest(unittest.TestCase):
    def setUp(self):
        self.server = ThreadingHTTPServer(('127.0.0.1', 0), pagamento_mock.PagamentoMock)
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.thread.start()
        self.url = f'http://127.0.0.1:{self.server.server_port}/pagamentos'
        self.payload = dict(pedidoId=str(uuid.uuid4()), canalPedido='APP', metodo='MOCK',
                            valor=53.82, moeda='BRL', aprovadoMock=True, numeroTentativa=1)

    def tearDown(self):
        self.server.shutdown()
        self.server.server_close()
        self.thread.join()

    def pagar(self, payload, caminho="/pagamentos"):
        chave = f"{payload['pedidoId']}:{payload['numeroTentativa']}" if caminho == '/pagamentos' else payload['pagamentoId'] + ':estorno'
        req = Request(self.url.replace('/pagamentos', caminho), data=json.dumps(payload).encode(),
                      headers={'Content-Type': 'application/json', 'Idempotency-Key': chave})
        try:
            with urlopen(req, timeout=2) as response:
                return response.status, json.load(response)
        except HTTPError as error:
            with error:
                return error.code, json.load(error)

    def test_aprovacao_e_repeticao_idempotente(self):
        status, primeiro = self.pagar(self.payload)
        self.assertEqual(201, status)
        self.assertEqual('APROVADO', primeiro['status'])
        status, segundo = self.pagar(self.payload)
        self.assertEqual(200, status)
        self.assertEqual(primeiro, segundo)
        self.payload['valor'] = 60
        self.assertEqual(409, self.pagar(self.payload)[0])

    def test_recusa(self):
        self.payload['aprovadoMock'] = False
        self.assertEqual('RECUSADO', self.pagar(self.payload)[1]['status'])

    def test_nova_tentativa_apos_recusa(self):
        self.payload['aprovadoMock'] = False
        primeiro = self.pagar(self.payload)[1]
        self.payload['aprovadoMock'] = True
        self.payload['numeroTentativa'] = 2
        status, segundo = self.pagar(self.payload)
        self.assertEqual(201, status)
        self.assertEqual('APROVADO', segundo['status'])
        self.assertNotEqual(primeiro['codigoTransacao'], segundo['codigoTransacao'])

    def test_estorno_idempotente(self):
        original = self.pagar(self.payload)[1]
        pedido = dict(pagamentoId=str(uuid.uuid4()), pedidoId=self.payload['pedidoId'],
                      codigoTransacao=original['codigoTransacao'], valor=self.payload['valor'], moeda='BRL')
        status, primeiro = self.pagar(pedido, '/estornos')
        self.assertEqual(201, status)
        self.assertEqual('ESTORNADO', primeiro['status'])
        status, segundo = self.pagar(pedido, '/estornos')
        self.assertEqual(200, status)
        self.assertEqual(primeiro, segundo)
        pedido['pagamentoId'] = str(uuid.uuid4())
        self.assertEqual(409, self.pagar(pedido, '/estornos')[0])

    def test_nao_estorna_pagamento_recusado(self):
        self.payload['aprovadoMock'] = False
        original = self.pagar(self.payload)[1]
        pedido = dict(pagamentoId=str(uuid.uuid4()), pedidoId=self.payload['pedidoId'],
                      codigoTransacao=original['codigoTransacao'], valor=self.payload['valor'], moeda='BRL')
        self.assertEqual(409, self.pagar(pedido, '/estornos')[0])

    def test_valores_invalidos(self):
        self.payload['valor'] = -1
        self.assertEqual(400, self.pagar(self.payload)[0])


if __name__ == '__main__':
    unittest.main()
