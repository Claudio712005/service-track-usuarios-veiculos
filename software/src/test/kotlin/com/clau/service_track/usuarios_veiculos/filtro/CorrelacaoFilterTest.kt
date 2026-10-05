package com.clau.service_track.usuarios_veiculos.filtro

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
class CorrelacaoFilterTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `devolve a correlacao informada e um identificador de requisicao proprio`() {
        val resposta = mockMvc.get("/usuarios") {
            header(CorrelacaoFilter.CABECALHO_CORRELACAO, "jornada-de-teste")
        }.andReturn().response

        assertAll(
            { assertEquals("jornada-de-teste", resposta.getHeader(CorrelacaoFilter.CABECALHO_CORRELACAO)) },
            { assertNotNull(resposta.getHeader(CorrelacaoFilter.CABECALHO_REQUISICAO)) },
        )
    }

    @Test
    fun `gera correlacao quando o cabecalho nao vem`() {
        val resposta = mockMvc.get("/usuarios").andReturn().response

        assertNotNull(resposta.getHeader(CorrelacaoFilter.CABECALHO_CORRELACAO))
    }

    @Test
    fun `cada requisicao recebe um identificador diferente`() {
        val primeira = mockMvc.get("/usuarios").andReturn().response
            .getHeader(CorrelacaoFilter.CABECALHO_REQUISICAO)
        val segunda = mockMvc.get("/usuarios").andReturn().response
            .getHeader(CorrelacaoFilter.CABECALHO_REQUISICAO)

        assertNotEquals(primeira, segunda)
    }

    @Test
    fun `descarta caractere perigoso da correlacao recebida`() {
        val resposta = mockMvc.get("/usuarios") {
            header(CorrelacaoFilter.CABECALHO_CORRELACAO, "jornada\n\"; DROP TABLE USUARIOS")
        }.andReturn().response

        val devolvida = resposta.getHeader(CorrelacaoFilter.CABECALHO_CORRELACAO)!!
        assertAll(
            { assertTrue(devolvida.all { it.isLetterOrDigit() || it == '-' || it == '_' }) },
            { assertTrue(devolvida.length <= 64) },
        )
    }

    @Test
    fun `corta correlacao longa no limite`() {
        val resposta = mockMvc.get("/usuarios") {
            header(CorrelacaoFilter.CABECALHO_CORRELACAO, "a".repeat(200))
        }.andReturn().response

        assertEquals(64, resposta.getHeader(CorrelacaoFilter.CABECALHO_CORRELACAO)!!.length)
    }
}
