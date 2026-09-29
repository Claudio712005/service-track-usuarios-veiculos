package com.clau.service_track.usuarios_veiculos.dto

import com.clau.service_track.usuarios_veiculos.entity.TipoDeUsuario
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(
    name = "CriarUsuarioRequest",
    description = "Dados para cadastrar um mecânico ou um cliente da oficina."
)
data class CriarUsuarioRequest(

    @field:NotBlank(message = "Documento é obrigatório")
    @get:Schema(
        description = "CPF, ou CNPJ numérico ou alfanumérico. A pontuação é opcional e as letras são " +
            "normalizadas para maiúsculas. Desde julho de 2026 as 12 primeiras posições do CNPJ " +
            "aceitam letra; os dois dígitos verificadores seguem numéricos.",
        example = "12.ABC.345/01DE-35",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val documento: String,

    @field:NotBlank(message = "Nome é obrigatório")
    @field:Size(max = 120, message = "Nome excede 120 caracteres")
    @get:Schema(
        description = "Nome completo da pessoa ou razão social.",
        example = "Joana Ferreira",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val nome: String,

    @field:NotBlank(message = "E-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    @field:Size(max = 255, message = "E-mail excede 255 caracteres")
    @get:Schema(
        description = "E-mail de contato, único no serviço.",
        example = "joana@oficina.com.br",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val email: String,

    @field:NotBlank(message = "Senha é obrigatória")
    @field:Size(min = 8, max = 72, message = "Senha deve ter de 8 a 72 caracteres")
    @get:Schema(
        description = "Senha em texto puro. Não volta em nenhuma resposta e não entra em log: " +
            "o serviço guarda apenas o hash bcrypt.",
        example = "troque-esta-senha",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val senha: String,

    @field:NotNull(message = "Tipo de usuário é obrigatório")
    @get:Schema(
        description = "MECANICO exige CPF, porque mecânico é pessoa física. CLIENTE aceita CPF ou CNPJ.",
        example = "CLIENTE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val tipoDeUsuario: TipoDeUsuario,

    @field:Size(max = 20, message = "Telefone excede 20 caracteres")
    @get:Schema(description = "Telefone de contato.", example = "11988887777")
    val telefone: String? = null,

    @get:Schema(
        description = "Papéis do usuário. Omitido, assume o papel com o mesmo nome do tipo.",
        example = "[\"CLIENTE\"]"
    )
    val roles: Set<String> = emptySet(),
)
