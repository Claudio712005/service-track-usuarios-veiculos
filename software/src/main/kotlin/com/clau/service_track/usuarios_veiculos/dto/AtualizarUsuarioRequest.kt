package com.clau.service_track.usuarios_veiculos.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(
    name = "AtualizarUsuarioRequest",
    description = "Campos alteráveis. Documento e tipo de usuário não mudam — para isso, cadastre outro."
)
data class AtualizarUsuarioRequest(

    @field:NotBlank(message = "Nome é obrigatório")
    @field:Size(max = 120, message = "Nome excede 120 caracteres")
    @get:Schema(
        description = "Nome completo ou razão social.",
        example = "Joana Ferreira Lima",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val nome: String,

    @field:NotBlank(message = "E-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    @get:Schema(
        description = "E-mail de contato, único no serviço.",
        example = "joana.lima@oficina.com.br",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val email: String,

    @field:Size(max = 20, message = "Telefone excede 20 caracteres")
    @get:Schema(description = "Telefone de contato.", example = "11988887777")
    val telefone: String? = null,

    @field:Size(min = 8, max = 72, message = "Senha deve ter de 8 a 72 caracteres")
    @get:Schema(description = "Informe apenas para trocar a senha. Ausente, a senha atual é mantida.")
    val senha: String? = null,

    @get:Schema(description = "Papéis do usuário. Vazio mantém os atuais.", example = "[\"CLIENTE\"]")
    val roles: Set<String>? = null,
)
