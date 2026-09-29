package com.clau.service_track.usuarios_veiculos.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(
    name = "AtualizarVeiculoRequest",
    description = "Campos alteráveis do veículo. Placa e dono não mudam aqui: transferência é outro caso de uso."
)
data class AtualizarVeiculoRequest(

    @field:NotBlank(message = "Marca é obrigatória")
    @field:Size(max = 40, message = "Marca excede 40 caracteres")
    @get:Schema(description = "Fabricante.", example = "Volkswagen", requiredMode = Schema.RequiredMode.REQUIRED)
    val marca: String,

    @field:NotBlank(message = "Modelo é obrigatório")
    @field:Size(max = 60, message = "Modelo excede 60 caracteres")
    @get:Schema(description = "Modelo.", example = "Gol 1.6 Highline", requiredMode = Schema.RequiredMode.REQUIRED)
    val modelo: String,

    @field:NotNull(message = "Ano do modelo é obrigatório")
    @field:Min(value = 1900, message = "Ano do modelo anterior a 1900")
    @field:Max(value = 2100, message = "Ano do modelo posterior a 2100")
    @get:Schema(description = "Ano do modelo.", example = "2022", requiredMode = Schema.RequiredMode.REQUIRED)
    val anoModelo: Int,

    @field:Size(max = 30, message = "Cor excede 30 caracteres")
    @get:Schema(description = "Cor predominante.", example = "Preto")
    val cor: String? = null,

    @get:Schema(description = "Chassi de 17 posições.", example = "9BWZZZ377VT004251")
    val chassi: String? = null,
)
