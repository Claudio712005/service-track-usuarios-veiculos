package com.clau.service_track.usuarios_veiculos.dominio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlacaTest {

    @Test
    fun `aceita o formato antigo com e sem hifen`() {
        assertTrue(Placa.valida("ABC1234"))
        assertTrue(Placa.valida("abc-1234"))
        assertEquals("ABC1234", Placa.exigirValida("abc-1234"))
    }

    @Test
    fun `aceita o formato mercosul`() {
        assertTrue(Placa.valida("RIO2A18"))
        assertEquals("RIO2A18", Placa.exigirValida("rio 2a18"))
    }

    @Test
    fun `recusa formato que nao existe`() {
        assertFalse(Placa.valida("AB12345"))
        assertFalse(Placa.valida("ABCD123"))
        assertFalse(Placa.valida("ABC12A3"))
        assertFalse(Placa.valida("ABC123"))
    }

    @Test
    fun `mensagem do erro cita os dois formatos`() {
        val erro = assertFailsWith<PlacaInvalidaException> { Placa.exigirValida("XX") }
        assertTrue(erro.message!!.contains("ABC1234"))
        assertTrue(erro.message!!.contains("ABC1D23"))
    }
}
