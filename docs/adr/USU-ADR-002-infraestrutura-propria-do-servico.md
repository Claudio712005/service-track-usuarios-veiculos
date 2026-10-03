# USU-ADR-002: infraestrutura própria do serviço

## Data
03/10/2026

## Status
Aceita.

---

## Contexto

A Fase 4 exige que cada microsserviço tenha **infraestrutura e banco próprios**, e o `IAC-ADR-026`
proíbe o repositório de plataforma de citar microsserviço. O que este serviço precisa provisionar na
AWS, então, mora aqui — do mesmo jeito que o catálogo já faz.

O serviço usa Postgres como fonte da verdade e Redis como cache da consulta por documento. Os dois
não têm o mesmo peso: perder o Postgres é perder dado; perder o Redis degrada a latência de uma
consulta e nada mais, porque o tratador de erro de cache registra em WARN e a consulta segue no banco.

## Decisão

### O que o Terraform deste repositório provisiona

| Recurso | Por quê |
|---|---|
| ECR `servicetrack-<ambiente>-usuarios-veiculos` | imagem do serviço, com tag imutável e expurgo de imagem sem tag em 7 dias |
| RDS Postgres `st_usu`, `db.t3.micro`, 20 GB gp3 | fonte da verdade do serviço |
| Security group do banco | 5432 aberto apenas para o CIDR da VPC da plataforma |
| DB subnet group e parameter group | subnets privadas descobertas por tag; `max_connections` declarado |
| Sete parâmetros no SSM | endpoint, porta, banco, usuário, senha, URL JDBC e URL do ECR |

Versão do Postgres é **apenas a maior** (`16`): versão menor fixa quebra o apply quando a AWS retira
a versão de circulação, numa data que ninguém controla.

### Redis fica fora do Terraform

O cache sobe como **pod efêmero no cluster**, não como ElastiCache. Três razões:

1. **O cache é descartável por desenho.** O serviço já trata a queda dele como degradação, e isso está
   coberto por teste. Pagar por um serviço gerenciado para um dado que pode evaporar a qualquer
   momento é comprar durabilidade que não é usada.
2. **Custo.** `cache.t3.micro` fica na ordem de US$ 12 por mês num orçamento total de US$ 50 por
   entrega, para guardar respostas com validade de dez minutos.
3. **Precedente.** O catálogo já roda o MongoDB como pod efêmero pelo mesmo motivo, e manter os dois
   serviços com a mesma regra — *fonte da verdade é gerenciada, apoio é efêmero* — vale mais que
   otimizar um caso.

Consequência aceita: a cada recriação de ambiente o cache nasce vazio. Isso não exige passo nenhum do
ritual, porque o cache se preenche na primeira consulta.

### Orçamento de conexões é verificado no plan

A instância carrega uma `precondition`:

```
db_pool_maximo × replicas_maximas + conexoes_reservadas ≤ db_max_connections
```

Com os valores de hoje: `10 × 4 + 10 = 50 ≤ 60`. Subir o teto do HPA sem subir o orçamento **falha o
plan**, com a conta na mensagem. É a regra 4 do projeto aplicada ao banco deste serviço: pool é
orçamento declarado, não ajuste local.

Verificado em execução, com a mesma expressão isolada: 4 réplicas passa, 5 passa no limite, 8 falha.

## Consequências

- O serviço passa a ter ECR e RDS próprios, e **nenhum acesso a banco de outro serviço** — o que
  encerra, para este contexto, a dívida `A-06`.
- O RDS vive **dentro da VPC da plataforma**, descoberta por tag. Isso cria uma ordem entre
  repositórios: rede antes do banco do serviço. E cria a armadilha do destroy — a VPC não é removida
  enquanto este RDS existir, e a guarda da esteira de plataforma hoje só procura o banco
  compartilhado.
- `db_username` e `db_password` não têm default: vêm dos secrets `ST_USU_DB_USER` e
  `ST_USU_DB_PASSWORD` do ambiente da esteira, com as mesmas validações do catálogo — minúsculas no
  usuário, 16 caracteres ou mais na senha, sem os caracteres que o RDS recusa.
- Falta a esteira. Terraform sem `infra.yml` só roda à mão; o CD e os manifestos do ArgoCD vêm em
  seguida.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Reusar o RDS compartilhado do `db-infra` | é exatamente o que a Fase 4 proíbe, e a dívida que este serviço existe para encerrar |
| ElastiCache para o Redis | paga durabilidade que o desenho não usa, em orçamento de US$ 50 |
| Postgres como pod efêmero, igual ao Mongo | é fonte da verdade: perder o volume é perder usuário e veículo cadastrados |
| `multi_az` e backup ligados | ambiente é destruído ao fim de cada teste; backup de dado que morre em horas é custo sem leitor |
| Serviço sem ECR próprio, publicando no ECR da plataforma | contraria o `IAC-ADR-026` e devolve ao `aws-iac` o conhecimento de microsserviço |
