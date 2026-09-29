package com.clau.service_track.usuarios_veiculos.dto

import com.clau.service_track.usuarios_veiculos.entity.TipoDeUsuario
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(
    name = "VerificacaoDeCredencialResponse",
    description = "Identidade confirmada. É o que a Lambda de autenticação precisa para emitir o JWT."
)
data class VerificacaoDeCredencialResponse(

    @get:Schema(description = "Identificador do usuário.", example = "018f2c9a-5f2e-7c31-9a41-6f3b2d0e9c11")
    val id: UUID,

    @get:Schema(description = "CPF ou CNPJ, sem pontuação. CNPJ pode conter letra.", example = "12ABC34501DE35")
    val documento: String,

    @get:Schema(description = "Nome completo ou razão social.", example = "Joana Ferreira")
    val nome: String,

    @get:Schema(description = "Se é mecânico ou cliente.", example = "CLIENTE")
    val tipoDeUsuario: TipoDeUsuario,

    @get:Schema(description = "Papéis que entram no claim do token.", example = "[\"CLIENTE\"]")
    val roles: Set<String>,
)
