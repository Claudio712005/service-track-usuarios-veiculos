package com.clau.service_track.usuarios_veiculos.controller

import com.clau.service_track.usuarios_veiculos.dto.AtualizarUsuarioRequest
import com.clau.service_track.usuarios_veiculos.dto.CriarUsuarioRequest
import com.clau.service_track.usuarios_veiculos.dto.UsuarioResponse
import com.clau.service_track.usuarios_veiculos.entity.TipoDeUsuario
import com.clau.service_track.usuarios_veiculos.excecao.ErroResponse
import com.clau.service_track.usuarios_veiculos.mapper.UsuarioMapper
import com.clau.service_track.usuarios_veiculos.service.UsuarioService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/usuarios")
@Tag(
    name = "Usuários",
    description = "Cadastro de mecânicos e clientes da oficina. Mecânico é pessoa física e exige CPF; " +
        "cliente aceita CPF ou CNPJ. Desativar preserva o histórico: nada é apagado."
)
class UsuarioController(
    private val servico: UsuarioService,
    private val mapper: UsuarioMapper,
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Lista os usuários ativos",
        description = "Ordenado por nome. Desativado não aparece aqui, mas segue consultável por identificador."
    )
    @ApiResponse(responseCode = "200", description = "Coleção retornada. Sem cadastro, responde 200 com lista vazia.")
    fun listar(
        @Parameter(description = "Filtra por tipo. Omita para trazer mecânicos e clientes.", example = "MECANICO")
        @RequestParam(name = "tipo", required = false)
        tipo: TipoDeUsuario?,
    ): List<UsuarioResponse> = mapper.paraResposta(servico.listar(tipo))

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Consulta um usuário por identificador")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado.")
    @ApiResponse(
        responseCode = "404",
        description = "Nenhum usuário com esse identificador.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun buscarPorId(
        @Parameter(description = "Identificador do usuário.", required = true) @PathVariable id: UUID,
    ): UsuarioResponse = mapper.paraResposta(servico.buscarPorId(id))

    @GetMapping("/por-documento", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Consulta um usuário por CPF ou CNPJ",
        description = "O documento vai como parâmetro de consulta, não no caminho: o CNPJ pontuado contém " +
            "uma barra, que o roteador leria como separador de segmento. Aceita com ou sem pontuação, e " +
            "letra minúscula no CNPJ alfanumérico. A resposta fica em cache por dez minutos, invalidado a " +
            "cada alteração ou desativação."
    )
    @ApiResponse(responseCode = "200", description = "Usuário encontrado.")
    @ApiResponse(
        responseCode = "404",
        description = "Nenhum usuário com esse documento.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun buscarPorDocumento(
        @Parameter(
            description = "CPF ou CNPJ, com ou sem pontuação.",
            example = "12.ABC.345/01DE-35",
            required = true
        )
        @RequestParam documento: String,
    ): UsuarioResponse = servico.consultarPorDocumento(documento)

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Cadastra um mecânico ou cliente",
        description = "Documento e e-mail são únicos. A senha é guardada apenas como hash bcrypt."
    )
    @ApiResponse(responseCode = "201", description = "Criado. O cabeçalho Location aponta para o recurso.")
    @ApiResponse(
        responseCode = "400",
        description = "Corpo inválido, documento que não é CPF nem CNPJ válido, ou mecânico com CNPJ.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    @ApiResponse(
        responseCode = "409",
        description = "Documento ou e-mail já cadastrado.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun criar(@Valid @RequestBody requisicao: CriarUsuarioRequest): ResponseEntity<UsuarioResponse> {
        val criado = servico.criar(requisicao)
        val endereco = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(criado.id)
            .toUri()
        return ResponseEntity.created(endereco).body(mapper.paraResposta(criado))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Atualiza nome, contato, senha ou papéis",
        description = "Documento e tipo de usuário não mudam: para isso, cadastre outro usuário."
    )
    @ApiResponse(responseCode = "200", description = "Usuário atualizado.")
    @ApiResponse(
        responseCode = "404",
        description = "Nenhum usuário com esse identificador.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    @ApiResponse(
        responseCode = "409",
        description = "E-mail já pertence a outro usuário.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody requisicao: AtualizarUsuarioRequest,
    ): UsuarioResponse = mapper.paraResposta(servico.atualizar(id, requisicao))

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Desativa o usuário",
        description = "Não apaga: desativa. A ordem de serviço histórica continua apontando para ele."
    )
    @ApiResponse(responseCode = "204", description = "Usuário desativado.")
    @ApiResponse(
        responseCode = "404",
        description = "Nenhum usuário com esse identificador.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    @ApiResponse(
        responseCode = "409",
        description = "Usuário já estava desativado.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun desativar(@PathVariable id: UUID): ResponseEntity<Void> {
        servico.desativar(id)
        return ResponseEntity.noContent().build()
    }
}
