package com.clau.service_track.usuarios_veiculos.dominio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChassiTest {

    @Test
    fun `aceita chassi de 17 posicoes`() {
        assertEquals("9BWZZZ377VT004251", Chassi.exigirValidoSeInformado("9BWZZZ377VT004251"))
    }

    @Test
    fun `normaliza caixa e pontuacao`() {
        assertEquals("9BWZZZ377VT004251", Chassi.exigirValidoSeInformado("9bw.zzz377-vt004251"))
    }

    @Test
    fun `ausente e valido`() {
        assertNull(Chassi.exigirValidoSeInformado(null))
        assertNull(Chassi.exigirValidoSeInformado("   "))
    }

    @Test
    fun `recusa as letras que a norma nao usa`() {
        val erro = assertFailsWith<ChassiInvalidoException> { Chassi.exigirValidoSeInformado("9BWZZZ377VT00425I") }
        assertTrue(erro.message!!.contains("I, O e Q"))
    }

    @Test
    fun `recusa tamanho diferente de 17`() {
        assertFailsWith<ChassiInvalidoException> { Chassi.exigirValidoSeInformado("9BWZZZ377VT0042") }
    }
}
