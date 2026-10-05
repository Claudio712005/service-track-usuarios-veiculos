# USU-ADR-001: padrão de log do serviço

## Data
02/10/2026

## Status
Aceita. Implementa o `GLOBAL-ADR-006`, que vale para todos os microsserviços.

---

## Contexto

O serviço já registrava `correlationId` e `requestId` no MDC, mas **não tinha rastro distribuído**:
nenhuma dependência de tracing, nenhum `traceId`. O log de acesso usava o caminho cru da requisição,
incluindo identificador de recurso, e a mensagem de documento inválido carregava o CPF ou o CNPJ
inteiro.

O `GLOBAL-ADR-006` fixou o padrão dos três identificadores e as obrigações de cada serviço. Este
documento registra como ele foi cumprido aqui, e o que foi medido em execução.

## Decisão

Cumprir as oito obrigações do `GLOBAL-ADR-006`:

| Obrigação | Como |
|---|---|
| rastro distribuído | `spring-boot-starter-opentelemetry` com `management.tracing.sampling.probability: 1.0` |
| sem ruído de exportador | `management.otlp.tracing.export.enabled: false` e o equivalente para métrica, até existir coletor |
| filtro por dentro da observação | `@Order(Ordered.HIGHEST_PRECEDENCE + 10)` no `CorrelacaoFilter` |
| uma linha por requisição | `requisicao concluida metodo= rota= status= duracaoMs=` |
| rota por template | `HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE`, com o caminho cru só como reserva |
| cabeçalhos devolvidos | `X-Correlation-Id` e `X-Request-Id` |
| formato | JSON em hml e prd, texto com `[serviço, traceId, spanId, correlationId, requestId]` em `dev` |
| documento mascarado | `Mascara.documento`, usado na mensagem de `DocumentoInvalidoException` |

`requestId` nunca é aceito de fora: é sempre gerado. `correlationId` é aceito do cabeçalho, saneado
para letra, dígito, hífen e sublinhado, e cortado em 64 caracteres.

## Evidência

Medido com o serviço em execução no perfil `dev`, não deduzido:

```
[service-track-usuarios-veiculos,f26f7679cdc67d2f72e7db1fe3802930,751405cc0ab4a806,teste-mdc,
 5db495e3-a23c-4566-855d-a6d3150adce3] requisicao concluida metodo=GET rota=/usuarios status=200
```

Dois detalhes que só apareceram ao medir, e que sozinhos deixariam o campo de rastro vazio:

1. **A chave no MDC é `traceId`, não `trace_id`.** Imprimindo as duas convenções lado a lado, só a
   caixa camelo tinha valor.
2. **A ordem do filtro decide.** Em `HIGHEST_PRECEDENCE`, a linha sai sem rastro mesmo com a chave
   certa, porque o escopo do span já fechou quando o `finally` registra. Em `+ 10`, sai com rastro.

## Consequências

- Uma consulta por `correlationId` no Grafana devolve a jornada; por `traceId`, o salto técnico.
- A rota no log passa a ser `/usuarios/{id}` em vez do caminho com o identificador: menos
  cardinalidade e nenhum identificador de recurso dentro da mensagem.
- A mensagem de documento inválido muda de texto: mostra `***4725` em vez do documento inteiro. O
  corpo da resposta de erro muda junto, e isso é desejado.
- Falta a parte da fila: publicar `traceparent` e `X-Correlation-Id` como cabeçalho da mensagem e
  reidratar o MDC no consumidor. Este serviço ainda não publica nem consome mensagem; quando passar a
  publicar, o `GLOBAL-ADR-006` já diz o que fazer.
