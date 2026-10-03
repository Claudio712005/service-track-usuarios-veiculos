# k8s/

Manifestos Kubernetes deste microsserviço. GitOps: o ArgoCD sincroniza a partir daqui, ninguém roda
`kubectl apply` na aplicação.

```
k8s/
├── base/                      Namespace, Deployment, Service ClusterIP, HPA
├── componentes/
│   └── redis-efemero/         Redis de dado descartável, usado em hml e prd
├── overlays/
│   ├── hml/                   ECR de hml, HPA 1..2
│   └── prd/                   ECR de prd, HPA 2..4
└── argocd/
    ├── hml.yaml               marcador de descoberta
    └── prd.yaml               marcador de descoberta
```

## De onde vem cada dependência

| Dependência | hml e prd |
|---|---|
| Postgres `st_usu` | RDS do próprio serviço, criado por `infra/terraform` |
| Redis | `componentes/redis-efemero`, pod no cluster |

**O Redis é efêmero de propósito** (`USU-ADR-002`): o serviço trata a queda dele como degradação —
registra WARN e a consulta segue no banco —, então pagar ElastiCache por durabilidade que o desenho
não usa não se justifica. A cada recriação de ambiente o cache nasce vazio e se preenche na primeira
consulta. Ele sobe com `--save ""`, sem persistência, teto de 64 MB e descarte das chaves menos
usadas: comportamento de cache, não de banco.

Não há overlay para `kind`. O ambiente local é o `docker-compose.yml` da raiz, com os perfis `h2` e
`postgres`.

## Credencial do banco não vem do ConfigMap

`ST_USU_DB_URL`, `ST_USU_DB_USER` e `ST_USU_DB_PASSWORD` vêm do Secret `service-track-usuarios-veiculos-db`,
que **não está neste repositório** e nunca estará. Ele é criado a partir do SSM, pela esteira:

```bash
scripts/criar-secret-do-banco.sh hml
```

O `secretRef` no `base/deployment.yaml` é `optional: true` para que o manifesto seja aplicável antes
de o Secret existir — o pod então fica reiniciando até a esteira Banco rodar, que é o esperado num
ambiente recém-criado.

## Ordem de subida do ambiente

```
1. rede e EKS            (service-track-aws-iac)
2. esteira Infra         (deste repo: ECR + RDS + SSM, e ela chama a esteira Banco)
3. esteira Banco         (Secret do banco, baseline do schema usuarios, restart da app)
4. esteira CD            (imagem no ECR + PR com a nova tag)
5. merge do PR           (ArgoCD sincroniza o overlay do ambiente)
```

Sem Flyway, `ddl-auto: validate` recusa banco vazio: é esperado ver `CrashLoopBackOff` até o passo 3
ter rodado. Não é defeito de manifesto.

## O registro da imagem não está escrito aqui

`newName` aponta para `ecr-do-ambiente`, que não existe. A URL do ECR carrega o identificador da conta
AWS, e essa conta muda a cada laboratório: quem escreve `newName` e `newTag` é a esteira CD, com o
valor que ela lê do SSM. Mesma regra do catálogo (`CAT-ADR-002`).

Enquanto o CD não rodar pela primeira vez, os pods ficam em `ImagePullBackOff`. É esperado.
