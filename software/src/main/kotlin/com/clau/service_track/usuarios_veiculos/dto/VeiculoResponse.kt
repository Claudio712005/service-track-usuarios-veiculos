package com.clau.service_track.usuarios_veiculos.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(name = "VeiculoResponse", description = "Veículo cadastrado, com o cliente a que pertence.")
data class VeiculoResponse(

    @get:Schema(description = "Identificador do veículo.", example = "018f4a02-31bc-7d55-9f21-7a0c4e2b6d10")
    val id: UUID,

    @get:Schema(description = "Cliente dono do veículo.", example = "018f2c9a-5f2e-7c31-9a41-6f3b2d0e9c11")
    val clienteId: UUID,

    @get:Schema(description = "Placa sem pontuação, em maiúsculas.", example = "RIO2A18")
    val placa: String,

    @get:Schema(description = "Fabricante.", example = "Volkswagen")
    val marca: String,

    @get:Schema(description = "Modelo.", example = "Gol 1.6")
    val modelo: String,

    @get:Schema(description = "Ano do modelo.", example = "2021")
    val anoModelo: Int,

    @get:Schema(description = "Cor predominante.", example = "Prata")
    val cor: String?,

    @get:Schema(description = "Chassi de 17 posições.", example = "9BWZZZ377VT004251")
    val chassi: String?,

    @get:Schema(description = "Veículo desativado sai da listagem padrão e não recebe ordem de serviço nova.", example = "true")
    val ativo: Boolean,

    @get:Schema(description = "Momento do cadastro, em UTC.", example = "2026-09-28T13:04:11Z")
    val dataCriacao: String,

    @get:Schema(description = "Momento da última alteração, em UTC.", example = "2026-09-28T13:40:02Z")
    val dataAtualizacao: String,
)
