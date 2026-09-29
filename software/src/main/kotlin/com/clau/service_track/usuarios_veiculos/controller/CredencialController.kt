package com.clau.service_track.usuarios_veiculos.controller

import com.clau.service_track.usuarios_veiculos.dto.VerificacaoDeCredencialRequest
import com.clau.service_track.usuarios_veiculos.dto.VerificacaoDeCredencialResponse
import com.clau.service_track.usuarios_veiculos.excecao.ErroResponse
import com.clau.service_track.usuarios_veiculos.mapper.UsuarioMapper
import com.clau.service_track.usuarios_veiculos.service.VerificacaoDeCredencialService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/credenciais")
@Tag(
    name = "Credenciais",
    description = "Conferência de documento e senha. É o que a Lambda de autenticação chama para emitir o JWT, " +
        "no lugar de ler a tabela de usuários direto."
)
class CredencialController(
    private val servico: VerificacaoDeCredencialService,
    private val mapper: UsuarioMapper,
) {

    @PostMapping(
        "/verificacao",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(
        summary = "Verifica documento e senha",
        description = "Responde 401 tanto para documento sem cadastro quanto para senha errada, de propósito: " +
            "distinguir os dois casos contaria a quem tenta se aquele CPF existe. O endpoint tem limite de " +
            "taxa; estourado, responde 429."
    )
    @ApiResponse(responseCode = "200", description = "Credencial confere; a resposta traz os papéis do usuário.")
    @ApiResponse(
        responseCode = "400",
        description = "Documento ou senha ausentes.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "Documento sem cadastro, usuário desativado ou senha incorreta.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    @ApiResponse(
        responseCode = "429",
        description = "Limite de tentativas atingido.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun verificar(
        @Valid @RequestBody requisicao: VerificacaoDeCredencialRequest,
    ): VerificacaoDeCredencialResponse =
        mapper.paraVerificacao(servico.verificar(requisicao.documento, requisicao.senha))
}
