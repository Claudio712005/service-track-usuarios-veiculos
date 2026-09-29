import base64
import json
import os
import sys
import time
import urllib.error
import urllib.request

BASE = os.environ.get("GRAFANA_URL", "http://grafana:3000")
USUARIO = os.environ.get("GRAFANA_USER", "admin")
SENHA = os.environ.get("GRAFANA_PASSWORD", "admin")

PLAYLISTS = [
    {
        "name": "Rodízio operacional",
        "interval": "1m",
        "items": [
            {"type": "dashboard_by_uid", "value": "usu-visao-geral", "order": 1, "title": ""},
            {"type": "dashboard_by_uid", "value": "usu-requisicoes", "order": 2, "title": ""},
            {"type": "dashboard_by_uid", "value": "usu-jvm", "order": 3, "title": ""},
            {"type": "dashboard_by_uid", "value": "usu-postgres", "order": 4, "title": ""},
        ],
    },
    {
        "name": "Plantão de incidente",
        "interval": "30s",
        "items": [
            {"type": "dashboard_by_uid", "value": "usu-visao-geral", "order": 1, "title": ""},
            {"type": "dashboard_by_uid", "value": "usu-logs", "order": 2, "title": ""},
            {"type": "dashboard_by_uid", "value": "usu-resiliencia", "order": 3, "title": ""},
        ],
    },
    {
        "name": "Tudo do serviço",
        "interval": "2m",
        "items": [
            {"type": "dashboard_by_tag", "value": "usuarios-veiculos", "order": 1, "title": ""},
        ],
    },
    {
        "name": "Infraestrutura e pilha",
        "interval": "1m",
        "items": [
            {"type": "dashboard_by_uid", "value": "usu-postgres", "order": 1, "title": ""},
            {"type": "dashboard_by_uid", "value": "usu-redis", "order": 2, "title": ""},
            {"type": "dashboard_by_uid", "value": "usu-pilha", "order": 3, "title": ""},
        ],
    },
]


def chama(caminho, metodo="GET", corpo=None):
    dados = json.dumps(corpo).encode() if corpo is not None else None
    pedido = urllib.request.Request(BASE + caminho, data=dados, method=metodo)
    credencial = base64.b64encode(f"{USUARIO}:{SENHA}".encode()).decode()
    pedido.add_header("Authorization", "Basic " + credencial)
    pedido.add_header("Content-Type", "application/json")
    with urllib.request.urlopen(pedido, timeout=10) as resposta:
        texto = resposta.read().decode()
        return json.loads(texto) if texto else None


def espera_grafana(tentativas=60):
    for _ in range(tentativas):
        try:
            if chama("/api/health").get("database") == "ok":
                return True
        except Exception:
            pass
        time.sleep(2)
    return False


def espera_dashboards(uids, tentativas=60):
    faltando = set(uids)
    for _ in range(tentativas):
        presentes = {d["uid"] for d in chama("/api/search?type=dash-db&limit=500")}
        faltando = set(uids) - presentes
        if not faltando:
            return []
        time.sleep(2)
    return sorted(faltando)


def main():
    if not espera_grafana():
        print("Grafana não respondeu; nenhuma playlist aplicada", file=sys.stderr)
        return 1

    esperados = [i["value"] for p in PLAYLISTS for i in p["items"]
                 if i["type"] == "dashboard_by_uid"]
    ausentes = espera_dashboards(set(esperados))
    if ausentes:
        print("provisionamento incompleto, faltam os painéis:", ", ".join(ausentes), file=sys.stderr)
        return 1

    existentes = {p["name"]: p["uid"] for p in chama("/api/playlists?limit=100") or []}
    for playlist in PLAYLISTS:
        nome = playlist["name"]
        if nome in existentes:
            chama(f"/api/playlists/{existentes[nome]}", "PUT", playlist)
            print("atualizada:", nome)
        else:
            chama("/api/playlists", "POST", playlist)
            print("criada:", nome)
    return 0


if __name__ == "__main__":
    sys.exit(main())
