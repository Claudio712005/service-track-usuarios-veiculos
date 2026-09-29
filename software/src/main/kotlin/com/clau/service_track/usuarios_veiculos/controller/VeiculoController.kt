package com.clau.service_track.usuarios_veiculos.controller

import com.clau.service_track.usuarios_veiculos.dto.AtualizarVeiculoRequest
import com.clau.service_track.usuarios_veiculos.dto.CriarVeiculoRequest
import com.clau.service_track.usuarios_veiculos.dto.VeiculoResponse
import com.clau.service_track.usuarios_veiculos.excecao.ErroResponse
import com.clau.service_track.usuarios_veiculos.mapper.VeiculoMapper
import com.clau.service_track.usuarios_veiculos.service.VeiculoService
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
@RequestMapping("/veiculos")
@Tag(
    name = "Veículos",
    description = "Frota dos clientes. Todo veículo pertence a um cliente ativo — mecânico não tem frota. " +
        "Desativar preserva o histórico de ordens de serviço."
)
class VeiculoController(
    private val servico: VeiculoService,
    private val mapper: VeiculoMapper,
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Lista os veículos ativos",
        description = "Ordenado por placa. Informe clienteId para ver só a frota daquele cliente."
    )
    @ApiResponse(responseCode = "200", description = "Coleção retornada, vazia quando não há cadastro.")
    @ApiResponse(
        responseCode = "404",
        description = "O clienteId informado não existe.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun listar(
        @Parameter(description = "Filtra pela frota de um cliente.", example = "018f2c9a-5f2e-7c31-9a41-6f3b2d0e9c11")
        @RequestParam(name = "clienteId", required = false)
        clienteId: UUID?,
    ): List<VeiculoResponse> = mapper.paraResposta(servico.listar(clienteId))

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Consulta um veículo por identificador")
    @ApiResponse(responseCode = "200", description = "Veículo encontrado.")
    @ApiResponse(
        responseCode = "404",
        description = "Nenhum veículo com esse identificador.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun buscarPorId(@PathVariable id: UUID): VeiculoResponse = mapper.paraResposta(servico.buscarPorId(id))

    @GetMapping("/por-placa", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Consulta um veículo por placa",
        description = "Aceita com ou sem hífen, maiúscula ou minúscula, no formato antigo ou Mercosul."
    )
    @ApiResponse(responseCode = "200", description = "Veículo encontrado.")
    @ApiResponse(
        responseCode = "404",
        description = "Nenhum veículo com essa placa.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun buscarPorPlaca(
        @Parameter(description = "Placa do veículo.", example = "RIO2A18", required = true)
        @RequestParam placa: String,
    ): VeiculoResponse = mapper.paraResposta(servico.buscarPorPlaca(placa))

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Cadastra um veículo para um cliente",
        description = "A placa é única. O dono precisa existir, ser cliente e estar ativo."
    )
    @ApiResponse(responseCode = "201", description = "Criado. O cabeçalho Location aponta para o recurso.")
    @ApiResponse(
        responseCode = "400",
        description = "Corpo inválido, placa fora dos formatos aceitos ou chassi inválido.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "O cliente informado não existe.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    @ApiResponse(
        responseCode = "409",
        description = "Placa já cadastrada, dono desativado, ou o usuário informado é mecânico.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun criar(@Valid @RequestBody requisicao: CriarVeiculoRequest): ResponseEntity<VeiculoResponse> {
        val criado = servico.criar(requisicao)
        val endereco = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(criado.id)
            .toUri()
        return ResponseEntity.created(endereco).body(mapper.paraResposta(criado))
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Atualiza marca, modelo, ano, cor ou chassi",
        description = "Placa e dono não mudam por aqui: transferência de veículo é outro caso de uso."
    )
    @ApiResponse(responseCode = "200", description = "Veículo atualizado.")
    @ApiResponse(
        responseCode = "404",
        description = "Nenhum veículo com esse identificador.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun atualizar(
        @PathVariable id: UUID,
        @Valid @RequestBody requisicao: AtualizarVeiculoRequest,
    ): VeiculoResponse = mapper.paraResposta(servico.atualizar(id, requisicao))

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa o veículo", description = "Não apaga: desativa, para o histórico continuar íntegro.")
    @ApiResponse(responseCode = "204", description = "Veículo desativado.")
    @ApiResponse(
        responseCode = "404",
        description = "Nenhum veículo com esse identificador.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    @ApiResponse(
        responseCode = "409",
        description = "Veículo já estava desativado.",
        content = [Content(schema = Schema(implementation = ErroResponse::class))]
    )
    fun desativar(@PathVariable id: UUID): ResponseEntity<Void> {
        servico.desativar(id)
        return ResponseEntity.noContent().build()
    }
}
