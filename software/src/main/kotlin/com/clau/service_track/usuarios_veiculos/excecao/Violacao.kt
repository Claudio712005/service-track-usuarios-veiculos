package com.clau.service_track.usuarios_veiculos.excecao

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Violacao", description = "Restrição violada em um atributo específico.")
data class Violacao(

    @get:Schema(description = "Atributo que falhou.", example = "email")
    val campo: String,

    @get:Schema(description = "Restrição violada, em linguagem de negócio.", example = "E-mail inválido")
    val mensagem: String,
)
