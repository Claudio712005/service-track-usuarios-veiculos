package com.clau.service_track.usuarios_veiculos.dominio

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MascaraTest {

    @Test
    fun `mostra apenas os quatro ultimos caracteres do cpf`() {
        assertEquals("***4725", Mascara.documento("529.982.247-25"))
    }

    @Test
    fun `mostra apenas os quatro ultimos caracteres do cnpj alfanumerico`() {
        assertEquals("***DE35", Mascara.documento("12.ABC.345/01DE-35"))
    }

    @Test
    fun `documento curto nao revela nada`() {
        assertEquals("***", Mascara.documento("123"))
        assertEquals("***", Mascara.documento(""))
        assertEquals("***", Mascara.documento(null))
    }

    @Test
    fun `documento invalido nao aparece inteiro na mensagem do erro`() {
        val erro = runCatching { Documento.exigirValido("52998224724") }.exceptionOrNull()!!

        assertTrue(erro.message!!.contains("***4724"))
        assertFalse(erro.message!!.contains("52998224724"))
    }
}
