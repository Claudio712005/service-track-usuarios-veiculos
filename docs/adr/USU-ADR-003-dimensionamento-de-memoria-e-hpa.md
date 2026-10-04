# USU-ADR-003: teto de heap abaixo do limite do contêiner, e HPA só por CPU

## Data
04/10/2026

## Status
Aceita. Registra a decisão já aplicada em `a36c92c`.

---

## Contexto

Na primeira subida deste serviço em `hml`, com o catálogo ao lado, a verificação do cluster
mostrou os dois HPAs presos no máximo de réplicas e este pod rente ao limite de memória:

```
usuarios   cpu: 7%/70%   memory: 109%/80%   min=1 max=2 atual=2
pod        381Mi usados   /   384Mi de limite
```

**3Mi de folga.** Sem nenhum reinício até então — por sorte, não por margem.

### A causa, medida

O `Dockerfile` declarava:

```
-XX:MaxRAMPercentage=75
-XX:InitialRAMPercentage=50
```

Conferido dentro do contêiner com `-XX:+PrintFlagsFinal`, num limite de 384Mi:

```
InitialHeapSize = 192 Mi
MaxHeapSize     = 288 Mi
```

Non-heap medido em `/actuator/prometheus` — metaspace, code cache, pilhas de thread, direct
buffers: **140Mi**.

```
288 (heap máximo permitido) + 140 (non-heap) = 428Mi  >  384Mi de limite
```

**A JVM estava autorizada a passar do próprio limite em 44Mi.** Não era limite apertado por
pouco: era configuração aritmeticamente impossível de respeitar.

### Reproduzido em kind, com a imagem real

Três variantes, 4.800 requisições em 6 conexões paralelas contra `/usuarios` e `/veiculos`:

| Configuração | heap pico | non-heap | RSS | limite | reinícios |
|---|---|---|---|---|---|
| 384Mi, MaxRAM 75% (como estava) | 108Mi | 140Mi | 332Mi | 384Mi | **2, `OOMKilled`, exit 137** |
| 1Gi, MaxRAM 75% | 201Mi | 165Mi | 477Mi | 1Gi | 0 |
| **640Mi, MaxRAM 60%** | 100Mi | 166Mi | **377Mi** | 640Mi | **0** |

A variante do meio mostra por que **subir só o limite não resolveria**: com
`MaxRAMPercentage=75` o heap acompanha o limite, e o estouro vai junto — 75% de 640Mi são 480Mi
de heap, mais 165Mi de non-heap, 645Mi outra vez acima. E `InitialRAMPercentage=50` faz o RSS
nascer colado no limite — 477Mi num limite de 1Gi —, o que por si só inflava a métrica de
memória do HPA.

### Sobre o HPA

A utilização de memória do HPA é medida contra **`requests`**, não contra o limite. Com
`requests: 320Mi` e uso real de 381Mi, a conta dava 119% contra um alvo de 80%: o HPA pedia o
máximo de réplicas permanentemente, e `minReplicas: 1` em `hml` era letra morta.

Registrado porque é contraintuitivo: **isso não significa que HPA por memória seja impossível
em JVM.** Medido no `kind` com `requests: 448Mi` e uso de 334Mi, a utilização fica em 74% e o
HPA permanece em 1 réplica, estável por quatro minutos sem carga. O que prendia no máximo era
`requests` abaixo do consumo real, não a métrica em si.

## Decisão

Três mudanças, que só funcionam juntas:

1. **`MaxRAMPercentage=60` e `InitialRAMPercentage=25`** no `Dockerfile`.
2. **`requests: 448Mi`, `limits: 640Mi`** no `base/deployment.yaml`.
3. **Métrica de memória removida do HPA**, em `base` e nos overlays de `hml` e `prd`. Fica CPU
   a 70%.

O motivo de (3) não é que a métrica não possa funcionar com (2) — pode, a 74% contra 80%. É que
**memória de JVM é praticamente constante**: sobe no aquecimento e não volta. Métrica que não
varia não carrega sinal de escala, e seis pontos percentuais de margem até o alvo desaparecem na
primeira dependência nova — aí o HPA volta a ficar preso no máximo sem ninguém perceber.

Verificado em `kind`: com CPU apenas, o HPA subiu para 2 sob carga e **voltou para 1** depois da
janela de estabilização. A configuração anterior nunca teve esse comportamento.

## Consequências

- **Fim do risco de OOMKill em demonstração.** O pico medido fica em 59% do limite novo.
- **O HPA volta a ser HPA.** Escala por CPU e desce quando a carga passa.
- Soma de `requests` de memória em `hml`, com os dois serviços em 2 réplicas, Mongo e Redis:
  cerca de 2.144Mi contra 7.249Mi alocáveis no nó `t3.large` — 30%, com 18 de 35 slots de pod
  ocupados. Sobra espaço.
- **Orçamento de conexão (`DB-ADR-004`) não muda:** `minReplicas`/`maxReplicas` seguem iguais, e
  o teto do orçamento é calculado sobre o máximo de réplicas.
- `IAC-ADR-023`, que dimensiona o nó, não muda.
- **Falta um teste que não foi feito:** nas medições do `kind`, as flags novas da JVM entraram
  por variável de ambiente, não vindas da imagem. Rebuildar a imagem com o `Dockerfile` novo e
  repetir a medição ficou pendente — o disco da máquina de teste encheu e corrompeu o ambiente
  de contêiner no meio do trabalho. A aritmética não depende disso, mas a confirmação de que a
  imagem publicada carrega as flags, sim.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Só subir o limite para 640Mi | `MaxRAMPercentage=75` leva o heap junto: 480+165 = 645Mi, estoura de novo |
| Só baixar `MaxRAMPercentage`, mantendo 384Mi | caberia (60% de 384 = 230Mi + 140 = 370Mi), com 14Mi de folga — a mesma fragilidade de hoje |
| Fixar `-Xmx` em valor absoluto | quebra quando o limite muda; a percentagem acompanha |
| Manter a métrica de memória e só corrigir `requests` | funciona hoje a 74%/80%, e volta a prender no máximo na primeira dependência nova |
| Trocar `UseSerialGC` por G1 | G1 tem mais overhead de memória; em contêiner pequeno com 2 vCPU o serial é adequado |
| Subir `maxReplicas` em vez de arrumar a memória | mais réplicas presas no máximo, gastando o dobro, sem resolver o OOM |
