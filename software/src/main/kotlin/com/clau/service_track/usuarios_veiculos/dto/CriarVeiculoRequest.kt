package com.clau.service_track.usuarios_veiculos.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

@Schema(
    name = "CriarVeiculoRequest",
    description = "Dados para cadastrar um veículo. O veículo pertence a um cliente ativo; mecânico não tem frota."
)
data class CriarVeiculoRequest(

    @field:NotNull(message = "Cliente é obrigatório")
    @get:Schema(
        description = "Identificador do cliente dono do veículo.",
        example = "018f2c9a-5f2e-7c31-9a41-6f3b2d0e9c11",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val clienteId: UUID,

    @field:NotBlank(message = "Placa é obrigatória")
    @get:Schema(
        description = "Placa no formato antigo ABC1234 ou Mercosul ABC1D23. Hífen e caixa são normalizados.",
        example = "RIO2A18",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val placa: String,

    @field:NotBlank(message = "Marca é obrigatória")
    @field:Size(max = 40, message = "Marca excede 40 caracteres")
    @get:Schema(description = "Fabricante.", example = "Volkswagen", requiredMode = Schema.RequiredMode.REQUIRED)
    val marca: String,

    @field:NotBlank(message = "Modelo é obrigatório")
    @field:Size(max = 60, message = "Modelo excede 60 caracteres")
    @get:Schema(description = "Modelo.", example = "Gol 1.6", requiredMode = Schema.RequiredMode.REQUIRED)
    val modelo: String,

    @field:NotNull(message = "Ano do modelo é obrigatório")
    @field:Min(value = 1900, message = "Ano do modelo anterior a 1900")
    @field:Max(value = 2100, message = "Ano do modelo posterior a 2100")
    @get:Schema(description = "Ano do modelo.", example = "2021", requiredMode = Schema.RequiredMode.REQUIRED)
    val anoModelo: Int,

    @field:Size(max = 30, message = "Cor excede 30 caracteres")
    @get:Schema(description = "Cor predominante.", example = "Prata")
    val cor: String? = null,

    @get:Schema(
        description = "Chassi de 17 posições, sem as letras I, O e Q, que a norma não usa para evitar confusão " +
            "com 1 e 0.",
        example = "9BWZZZ377VT004251"
    )
    val chassi: String? = null,
)
