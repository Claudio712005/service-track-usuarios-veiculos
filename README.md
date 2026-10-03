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

Log em JSON no console, com `correlationId` e `requestId` no MDC. A correlação vem do cabeçalho
`X-Correlation-Id` quando existe, é gerada quando não existe, e volta na resposta.

O compose sobe a pilha local:

| Serviço | Endereço | Papel |
|---|---|---|
| Grafana | http://localhost:3000 | visualização, `admin` / `admin` |
| Loki | http://localhost:3100 | logs |
| Prometheus | http://localhost:9090 | métricas de `/actuator/prometheus` |
| Alloy | http://localhost:12345 | coleta os logs do contêiner e envia ao Loki |

O Grafana entra **limpo**, apenas com as fontes de dados provisionadas: painel e alerta são
criados por quem opera.

---

## Testes e cobertura

```bash
cd software && ./gradlew build
```

`build` roda os testes **e o portão de cobertura**: abaixo do mínimo, o build falha. Medição de
03/10/2026, sem exclusão de pacote nenhuma:

| Medida | Atual | Mínimo exigido pelo portão |
|---|---|---|
| Linha | **90,9%** | 80% |
| Instrução | **84,8%** | 80% |
| Ramo | 65,4% | 60% |
| Método | 89,3% | — |

O portão de ramo está em 60% de propósito: é onde o código está hoje, e subir para 80% exige teste de
caminho de erro que ainda não existe. O número publicado aqui é o real, não o conveniente — e o
portão existe para impedir regressão, não para fingir que já chegamos.

Onde a cobertura é mais baixa: `config` (47,9%, definição de bean), `excecao` (68,1%, caminhos de erro
menos exercitados) e `filtro` (75,6%).

## Como este serviço chega em hml e prd

```
1. rede e EKS            (service-track-aws-iac)
2. esteira Infra         (deste repo: ECR + RDS + SSM, e ela chama a esteira Banco)
3. esteira Banco         (Secret do banco a partir do SSM, baseline do schema, restart)
4. esteira CD            (imagem no ECR + PR com a nova tag)
5. merge do PR           (ArgoCD sincroniza o overlay do ambiente)
```

Detalhe de cada passo e das dependências em [k8s/README.md](k8s/README.md). A infraestrutura AWS do
serviço vive em [infra/terraform/](infra/terraform/) e a decisão por trás dela em
[`USU-ADR-002`](docs/adr/USU-ADR-002-infraestrutura-propria-do-servico.md).

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
