package com.clau.service_track.usuarios_veiculos.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(
    name = "VerificacaoDeCredencialRequest",
    description = "Documento e senha para conferência. Vai no corpo, nunca na URL: query string " +
        "aparece em log de servidor, em histórico de proxy e no cabeçalho Referer."
)
data class VerificacaoDeCredencialRequest(

    @field:NotBlank(message = "Documento é obrigatório")
    @get:Schema(
        description = "CPF ou CNPJ, com ou sem pontuação.",
        example = "529.982.247-25",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val documento: String,

    @field:NotBlank(message = "Senha é obrigatória")
    @get:Schema(
        description = "Senha em texto puro, conferida contra o hash bcrypt.",
        example = "troque-esta-senha",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val senha: String,
)
