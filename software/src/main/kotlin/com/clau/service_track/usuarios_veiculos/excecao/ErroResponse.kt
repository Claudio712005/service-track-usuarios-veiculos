package com.clau.service_track.usuarios_veiculos.excecao

import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime

@Schema(
    name = "ErroResponse",
    description = "Corpo devolvido em toda resposta de erro deste serviço. O campo codigo é estável " +
        "e serve para tratamento programático; a mensagem é para leitura humana."
)
data class ErroResponse(

    @get:Schema(description = "Momento do erro, em UTC.", example = "2026-09-28T13:04:11.482Z")
    val timestamp: OffsetDateTime,

    @get:Schema(description = "Código de estado HTTP.", example = "409")
    val status: Int,

    @get:Schema(description = "Classificação estável do erro.", example = "CONFLITO")
    val codigo: String,

    @get:Schema(description = "Explicação legível, sem detalhe de implementação.", example = "Documento já cadastrado")
    val mensagem: String,

    @get:Schema(description = "Caminho que originou o erro.", example = "/usuarios")
    val caminho: String,

    @get:Schema(description = "Violações por atributo. Presente apenas em erro de validação.")
    val violacoes: List<Violacao>? = null,
)
