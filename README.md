# service-track-usuarios-veiculos

Microsserviço de **mecânicos, clientes e veículos** da plataforma ServiceTrack.

É dono dos dados de usuário da oficina: cadastro, consulta, desativação e a verificação de
credencial usada por quem emite o token. Não emite token, não conhece ordem de serviço e não lê
o banco de nenhum outro serviço.

Arquitetura em camadas (MVC): `controller` → `service` → `repository` → `entity`, com
`dominio` para as regras de validação de documento, placa e chassi.

---

## O que este serviço expõe

| Método | Rota | Para quê |
|---|---|---|
| `POST` | `/usuarios` | cadastra mecânico ou cliente |
| `GET` | `/usuarios` | lista ativos, com filtro opcional `tipoDeUsuario` |
| `GET` | `/usuarios/{id}` | busca por identificador |
| `GET` | `/usuarios/por-documento?documento=` | busca por CPF ou CNPJ |
| `PUT` | `/usuarios/{id}` | atualiza dados cadastrais |
| `DELETE` | `/usuarios/{id}` | desativa (não apaga) |
| `POST` | `/credenciais/verificacao` | confere documento e senha |
| `POST` | `/veiculos` | cadastra veículo de um cliente ativo |
| `GET` | `/veiculos` | lista ativos, com filtro opcional `clienteId` |
| `GET` | `/veiculos/{id}` | busca por identificador |
| `GET` | `/veiculos/por-placa?placa=` | busca por placa |
| `PUT` | `/veiculos/{id}` | atualiza dados do veículo |
| `DELETE` | `/veiculos/{id}` | desativa |

Documentação navegável em `/swagger-ui.html`, contrato em `/v3/api-docs`.

**A busca por documento é parâmetro de consulta, não path.** CNPJ pontuado contém `/`, que
quebra o roteamento por caminho.

### Regras de domínio que o serviço garante

- **CPF**: 11 dígitos com dígitos verificadores conferidos.
- **CNPJ alfanumérico**: 12 posições alfanuméricas e 2 dígitos verificadores numéricos, com o
  valor de cada caractere calculado como ASCII menos 48, conforme a regra em vigor desde
  julho de 2026. CNPJ numérico continua valendo — é um caso particular.
- **Mecânico é pessoa física**: aceita apenas CPF. Cliente aceita CPF ou CNPJ.
- **Placa**: padrão antigo `ABC1234` e Mercosul `ABC1D23`.
- **Chassi**: 17 posições, sem `I`, `O` e `Q`.
- **Veículo pertence a cliente ativo.** Mecânico não tem veículo.
- **Desativação é lógica.** Nada é apagado; unicidade de documento, e-mail e placa vale entre
  os registros ativos.
- **Senha** é guardada apenas como hash bcrypt e nunca aparece em resposta ou em log.

---

## Rodar local

Tudo com Docker, a partir da raiz do repositório. Dois perfis, mutuamente exclusivos.

### Perfil `h2` — só a aplicação

```bash
docker compose --profile h2 up -d --build
```

Banco em memória, cache local, sem Redis. Console do H2 em `/h2`. É o caminho mais rápido para
exercitar a API.

### Perfil `postgres` — aplicação, Postgres e Redis

```bash
docker compose --profile postgres up -d --build
```

O Postgres aplica `db/postgres/01_baseline_st_usu.sql` na criação do volume, e a aplicação sobe
com `ddl-auto: validate` — se o baseline e as entidades divergirem, o serviço não sobe. É esse
o objetivo.

Derrubar, incluindo os volumes:

```bash
docker compose --profile postgres --profile h2 down -v
```

### Sem Docker

```bash
cd software && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Testes

```bash
cd software && ./gradlew test
```

---

## Configuração

O perfil padrão (`application.yaml`) atende hml e prd e **não tem valor de banco embutido**:

| Variável | Obrigatória | Para quê |
|---|---|---|
| `ST_USU_DB_URL` | sim | JDBC do Postgres |
| `ST_USU_DB_USER` | sim | usuário de runtime |
| `ST_USU_DB_PASSWORD` | sim | senha do usuário de runtime |
| `ST_USU_DB_POOL_MAX` | não (10) | teto do pool Hikari — é orçamento de conexões, não ajuste local |
| `ST_USU_DB_POOL_MIN` | não (2) | conexões ociosas mantidas |
| `ST_USU_REDIS_HOST` | não (`redis`) | host do cache |
| `ST_USU_REDIS_PORT` | não (6379) | porta do cache |
| `ST_USU_LIMITE_VERIFICACAO` | não (20) | verificações de credencial por segundo |
| `ST_USU_FORMATO_DE_LOG` | não (`logstash`) | formato do log no console |

O perfil `dev` é sobreposição: H2 em memória, cache local, log legível e indicador de saúde do
Redis desligado.

### Schema do banco

O serviço usa o schema `usuarios` e **não gerencia migrations**: o schema é o baseline em
`db/postgres/`, e o Hibernate apenas valida contra ele. Restrições de formato de documento,
placa e chassi existem também no banco, não só na aplicação.

---

## Observabilidade

### Padrão de log

Quatro identificadores em toda linha, conforme `GLOBAL-ADR-006` e `USU-ADR-001`:

| Campo | De onde vem | O que identifica |
|---|---|---|
| `traceId` / `spanId` | instrumentação, propagado por `traceparent` | o salto técnico entre processos |
| `correlationId` | cabeçalho `X-Correlation-Id`, ou gerado | a operação de negócio inteira |
| `requestId` | sempre gerado pelo serviço | esta requisição |

`requestId` nunca é aceito de fora: valor repetido confundiria duas requisições no log, e o campo
existe para distingui-las. Correlação é o contrário — repetir é o objetivo.

A resposta devolve `X-Correlation-Id` e `X-Request-Id`, então quem chamou acha a própria requisição
no log sem adivinhar.

Uma linha por requisição, no fim dela, com a **rota em template** (`/usuarios/{id}`), nunca o caminho
com identificador. Nível pelo resultado: ERROR em 5xx, WARN em 4xx, INFO em escrita, DEBUG em
leitura. `actuator`, `swagger` e `api-docs` ficam fora.

Em `dev` o padrão é `[serviço, traceId, spanId, correlationId, requestId]`; em hml e prd o formato é
JSON e os campos vão como propriedades.

**Documento nunca aparece inteiro no log nem em mensagem de erro**: `Mascara.documento` deixa só os
quatro últimos caracteres (`***4725`). Senha, hash e token não são registrados em nenhuma hipótese.

O compose sobe a pilha local:

| Serviço | Endereço | Papel |
|---|---|---|
| Grafana | http://localhost:3000 | visualização, `admin` / `admin` |
| Loki | http://localhost:3100 | logs |
| Prometheus | http://localhost:9090 | métricas |
| Alloy | http://localhost:12345 | coleta os logs do contêiner e envia ao Loki |
| Exportador do Redis | http://localhost:9121 | métricas do cache (perfil `postgres`) |
| Exportador do Postgres | http://localhost:9187 | métricas do banco (perfil `postgres`) |
| Receptor de alerta | http://localhost:8082 | registra no log a notificação que o Grafana envia |

Tudo é provisionado por arquivo em `observabilidade/grafana/`. Subir o compose já entrega
fontes de dados, painéis, alertas e playlists — não há passo manual na interface.

### Painéis

| Pasta | Painel | Para quê |
|---|---|---|
| `servico` | **visão geral** | saúde, taxa, erro, latência e cache numa tela; é a tela inicial |
| `servico` | **requisições** | rota a rota, alvos de tempo de resposta, erros e uso do repositório |
| `servico` | **JVM e processo** | memória, coleta de lixo, threads, CPU e disco |
| `servico` | **logs e correlação** | volume por nível, origens e o rastro de uma correlação |
| `servico` | **resiliência e cache** | limitador de taxa, cache Redis e prova de degradação |
| `infra` | **Postgres e pool** | banco e o orçamento de conexões do Hikari |
| `infra` | **Redis** | memória, chaves, taxa de acerto e comandos |
| `plataforma` | **saúde da pilha** | alvos de coleta, Prometheus, Loki, Alloy e Grafana |

O provedor sobe com `allowUiUpdates`, então dá para editar na interface. Para a mudança
sobreviver a um `down -v`, exportar o JSON e sobrescrever o arquivo em
`observabilidade/grafana/dashboards/`.

### Playlists

Playlist não tem provisionamento por arquivo no Grafana, então o serviço `grafana-playlists`
aplica quatro por API, de forma idempotente, e encerra:

| Playlist | Giro | Conteúdo |
|---|---|---|
| Rodízio operacional | 1 min | visão geral, requisições, JVM, Postgres |
| Plantão de incidente | 30 s | visão geral, logs, resiliência |
| Tudo do serviço | 2 min | todos os painéis com a etiqueta `usuarios-veiculos` |
| Infraestrutura e pilha | 1 min | Postgres, Redis, saúde da pilha |

Reaplicar depois de mexer nelas: `docker compose --profile postgres up grafana-playlists`.

### Alertas

Onze regras na pasta **Alertas do serviço**, avaliadas a cada minuto: serviço fora do ar, erro
5xx acima de 5%, p95 acima de 1s, pool do banco com requisição em espera, heap acima de 85%,
limitador barrando, erro no log, cache indisponível, Redis fora, Postgres fora e reinício do
processo.

A notificação vai para um receptor local que **imprime o alerta no próprio log**, que o Alloy
coleta — dá para ver o alerta chegando dentro do Grafana. Rota de verdade (e-mail, Slack,
plantão) é decisão de quando o serviço for para o cluster, não de ambiente local.

As regras de Redis e Postgres ficam sem dado no perfil `h2`, onde esses serviços não existem.
É de propósito: `noDataState` delas é `OK`, então não alertam por ausência.

### O que ainda não é observado

- **Não há trace distribuído.** Nem OpenTelemetry na aplicação, nem Tempo na pilha. É requisito
  da Fase 4 e entra junto com a mensageria, porque só então há um salto entre serviços para
  rastrear.
- **Cache e limitador não publicam métrica própria.** O Micrometer não instrumenta `RedisCache`,
  e o limitador é um `RateLimiter` avulso, fora de um registry. A taxa de acerto do cache vem do
  exportador do Redis e o limitador é medido pelas respostas 429 — que é observação por efeito,
  não por instrumentação. Fechar isso é mudança na aplicação: registrar o limitador num
  `RateLimiterRegistry` e expor as estatísticas do cache.
- **Sem métrica por contêiner.** O cadvisor foi testado e no colima só enxerga o cgroup raiz,
  então saiu da pilha em vez de entregar painel vazio.

---

## Resiliência

- **Queda do Redis não derruba requisição.** A falha de cache é registrada em WARN e a consulta
  segue no banco.
- **Verificação de credencial tem limitador de taxa** e devolve 429 no estouro.
- **Documento inexistente compara contra um hash fixo**, para que o tempo de resposta não
  revele se o documento existe.

---

## Fronteiras

Este repositório é dono do código, do baseline do schema e da imagem do serviço.

Não é dono de: emissão de token, infraestrutura compartilhada da AWS, API Gateway, e dados de
qualquer outro contexto. Precisar de dado alheio é chamar a API do dono ou consumir um evento
dele — nunca ler o banco do outro.
