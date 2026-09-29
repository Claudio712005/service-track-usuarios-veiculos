package com.clau.service_track.usuarios_veiculos.excecao

import com.clau.service_track.usuarios_veiculos.dominio.ChassiInvalidoException
import com.clau.service_track.usuarios_veiculos.dominio.DocumentoInvalidoException
import com.clau.service_track.usuarios_veiculos.dominio.PlacaInvalidaException
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import jakarta.servlet.http.HttpServletRequest
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import tools.jackson.databind.exc.MismatchedInputException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class TratadorGlobalDeErros {

    private val log = LoggerFactory.getLogger(TratadorGlobalDeErros::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun corpoInvalido(e: MethodArgumentNotValidException, requisicao: HttpServletRequest): ResponseEntity<ErroResponse> {
        val violacoes = e.bindingResult.fieldErrors.map { Violacao(it.field, it.defaultMessage ?: "valor inválido") }
        return montarResposta(
            HttpStatus.BAD_REQUEST,
            "REQUISICAO_INVALIDA",
            "Corpo da requisição contém ${violacoes.size} campo(s) inválido(s)",
            requisicao,
            violacoes,
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun corpoIlegivel(e: HttpMessageNotReadableException, requisicao: HttpServletRequest): ResponseEntity<ErroResponse> {
        val campos = camposFaltantes(e)
        if (campos.isEmpty()) {
            return montarResposta(
                HttpStatus.BAD_REQUEST,
                "CORPO_ILEGIVEL",
                "Corpo da requisição não é um JSON válido",
                requisicao,
            )
        }
        val violacoes = campos.map { Violacao(it, "campo obrigatório ausente ou de tipo incompatível") }
        return montarResposta(
            HttpStatus.BAD_REQUEST,
            "REQUISICAO_INVALIDA",
            "Corpo da requisição contém ${violacoes.size} campo(s) inválido(s)",
            requisicao,
            violacoes,
        )
    }

    private fun camposFaltantes(e: HttpMessageNotReadableException): List<String> {
        val causa = generateSequence(e.cause) { it.cause }.filterIsInstance<MismatchedInputException>().firstOrNull()
        return causa?.path?.mapNotNull { it.propertyName }?.filter { it.isNotBlank() } ?: emptyList()
    }

    @ExceptionHandler(DocumentoInvalidoException::class)
    fun documentoInvalido(e: DocumentoInvalidoException, requisicao: HttpServletRequest) = montarResposta(
        HttpStatus.BAD_REQUEST,
        "DOCUMENTO_INVALIDO",
        e.message ?: "Documento inválido",
        requisicao,
    )

    @ExceptionHandler(PlacaInvalidaException::class)
    fun placaInvalida(e: PlacaInvalidaException, requisicao: HttpServletRequest) = montarResposta(
        HttpStatus.BAD_REQUEST,
        "PLACA_INVALIDA",
        e.message ?: "Placa inválida",
        requisicao,
    )

    @ExceptionHandler(ChassiInvalidoException::class)
    fun chassiInvalido(e: ChassiInvalidoException, requisicao: HttpServletRequest) = montarResposta(
        HttpStatus.BAD_REQUEST,
        "CHASSI_INVALIDO",
        e.message ?: "Chassi inválido",
        requisicao,
    )

    @ExceptionHandler(ConflitoException::class)
    fun conflito(e: ConflitoException, requisicao: HttpServletRequest) = montarResposta(
        HttpStatus.CONFLICT,
        "CONFLITO",
        e.message ?: "Conflito de estado",
        requisicao,
    )

    @ExceptionHandler(RecursoNaoEncontradoException::class)
    fun naoEncontrado(e: RecursoNaoEncontradoException, requisicao: HttpServletRequest) = montarResposta(
        HttpStatus.NOT_FOUND,
        "RECURSO_NAO_ENCONTRADO",
        e.message ?: "Recurso não encontrado",
        requisicao,
    )

    @ExceptionHandler(CredencialInvalidaException::class)
    fun credencialInvalida(e: CredencialInvalidaException, requisicao: HttpServletRequest) = montarResposta(
        HttpStatus.UNAUTHORIZED,
        "CREDENCIAL_INVALIDA",
        e.message ?: "Credencial inválida",
        requisicao,
    )

    @ExceptionHandler(RequestNotPermitted::class)
    fun excessoDeTentativas(e: RequestNotPermitted, requisicao: HttpServletRequest): ResponseEntity<ErroResponse> {
        log.warn("limite de verificação de credencial atingido caminho={}", requisicao.requestURI)
        return montarResposta(
            HttpStatus.TOO_MANY_REQUESTS,
            "EXCESSO_DE_TENTATIVAS",
            "Limite de tentativas atingido. Tente novamente em instantes",
            requisicao,
        )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun rotaInexistente(e: NoResourceFoundException, requisicao: HttpServletRequest) = montarResposta(
        HttpStatus.NOT_FOUND,
        "ROTA_INEXISTENTE",
        "Nenhum recurso mapeado para este caminho",
        requisicao,
    )

    @ExceptionHandler(Exception::class)
    fun naoPrevisto(e: Exception, requisicao: HttpServletRequest): ResponseEntity<ErroResponse> {
        log.error("falha não prevista em {}", requisicao.requestURI, e)
        return montarResposta(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "ERRO_INTERNO",
            "Falha não prevista ao processar a requisição",
            requisicao,
        )
    }

    private fun montarResposta(
        status: HttpStatus,
        codigo: String,
        mensagem: String,
        requisicao: HttpServletRequest,
        violacoes: List<Violacao>? = null,
    ): ResponseEntity<ErroResponse> = ResponseEntity.status(status).body(
        ErroResponse(
            timestamp = OffsetDateTime.now(ZoneOffset.UTC),
            status = status.value(),
            codigo = codigo,
            mensagem = mensagem,
            caminho = requisicao.requestURI,
            violacoes = violacoes,
        )
    )
}
