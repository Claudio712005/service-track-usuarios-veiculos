package com.clau.service_track.usuarios_veiculos.dto

import com.clau.service_track.usuarios_veiculos.entity.TipoDeDocumento
import com.clau.service_track.usuarios_veiculos.entity.TipoDeUsuario
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(
    name = "UsuarioResponse",
    description = "Usuário cadastrado. A senha nunca aparece aqui, nem em hash."
)
data class UsuarioResponse(

    @get:Schema(description = "Identificador do usuário.", example = "018f2c9a-5f2e-7c31-9a41-6f3b2d0e9c11")
    val id: UUID,

    @get:Schema(description = "CPF ou CNPJ, sem pontuação. CNPJ pode conter letra.", example = "12ABC34501DE35")
    val documento: String,

    @get:Schema(description = "Tipo do documento, derivado do tamanho.", example = "CPF")
    val tipoDeDocumento: TipoDeDocumento,

    @get:Schema(description = "Nome completo ou razão social.", example = "Joana Ferreira")
    val nome: String,

    @get:Schema(description = "E-mail de contato.", example = "joana@oficina.com.br")
    val email: String,

    @get:Schema(description = "Se é mecânico ou cliente.", example = "CLIENTE")
    val tipoDeUsuario: TipoDeUsuario,

    @get:Schema(description = "Telefone de contato.", example = "11988887777")
    val telefone: String?,

    @get:Schema(
        description = "Usuário desativado não autentica e sai da listagem padrão.",
        example = "true"
    )
    val ativo: Boolean,

    @get:Schema(description = "Papéis usados na emissão do token.", example = "[\"CLIENTE\"]")
    val roles: Set<String>,

    @get:Schema(description = "Momento do cadastro, em UTC.", example = "2026-09-28T13:04:11Z")
    val dataCriacao: String,

    @get:Schema(description = "Momento da última alteração, em UTC.", example = "2026-09-28T13:40:02Z")
    val dataAtualizacao: String,
)
