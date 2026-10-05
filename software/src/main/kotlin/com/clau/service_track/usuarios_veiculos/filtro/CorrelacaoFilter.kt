package com.clau.service_track.usuarios_veiculos.filtro

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.HandlerMapping

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class CorrelacaoFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(CorrelacaoFilter::class.java)

    override fun doFilterInternal(
        requisicao: HttpServletRequest,
        resposta: HttpServletResponse,
        cadeia: FilterChain,
    ) {
        val correlacao = somenteCaracteresSeguros(requisicao.getHeader(CABECALHO_CORRELACAO))
            ?: UUID.randomUUID().toString()
        val requisicaoId = UUID.randomUUID().toString()

        MDC.put(CHAVE_CORRELACAO, correlacao)
        MDC.put(CHAVE_REQUISICAO, requisicaoId)
        resposta.setHeader(CABECALHO_CORRELACAO, correlacao)
        resposta.setHeader(CABECALHO_REQUISICAO, requisicaoId)

        val inicio = System.nanoTime()
        try {
            cadeia.doFilter(requisicao, resposta)
        } finally {
            registrarRequisicao(requisicao, resposta, (System.nanoTime() - inicio) / 1_000_000)
            MDC.remove(CHAVE_CORRELACAO)
            MDC.remove(CHAVE_REQUISICAO)
        }
    }

    override fun shouldNotFilter(requisicao: HttpServletRequest): Boolean =
        CAMINHOS_SEM_LOG.any { requisicao.requestURI.startsWith(it) }

    private fun registrarRequisicao(
        requisicao: HttpServletRequest,
        resposta: HttpServletResponse,
        duracaoEmMs: Long,
    ) {
        val metodo = requisicao.method
        val rota = rotaDe(requisicao)
        val situacao = resposta.status
        when {
            situacao >= 500 -> log.error(FORMATO, metodo, rota, situacao, duracaoEmMs)
            situacao >= 400 -> log.warn(FORMATO, metodo, rota, situacao, duracaoEmMs)
            metodo == "GET" -> log.debug(FORMATO, metodo, rota, situacao, duracaoEmMs)
            else -> log.info(FORMATO, metodo, rota, situacao, duracaoEmMs)
        }
    }

    private fun somenteCaracteresSeguros(bruto: String?): String? = bruto
        ?.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        ?.take(TAMANHO_MAXIMO)
        ?.ifBlank { null }

    companion object {

        const val CABECALHO_CORRELACAO = "X-Correlation-Id"
        const val CABECALHO_REQUISICAO = "X-Request-Id"
        const val CHAVE_CORRELACAO = "correlationId"
        const val CHAVE_REQUISICAO = "requestId"

        private const val TAMANHO_MAXIMO = 64
        private const val FORMATO = "requisicao concluida metodo={} rota={} status={} duracaoMs={}"
        private val CAMINHOS_SEM_LOG = listOf("/actuator", "/v3/api-docs", "/swagger-ui", "/h2")

        fun rotaDe(requisicao: HttpServletRequest): String =
            requisicao.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String
                ?: requisicao.requestURI
    }
}
