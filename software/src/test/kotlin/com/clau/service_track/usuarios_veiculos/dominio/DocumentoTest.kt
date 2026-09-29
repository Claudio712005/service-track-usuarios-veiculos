package com.clau.service_track.usuarios_veiculos.dominio

import com.clau.service_track.usuarios_veiculos.entity.TipoDeDocumento
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DocumentoTest {

    @Test
    fun `aceita cpf valido com e sem pontuacao`() {
        assertTrue(Documento.valido("529.982.247-25"))
        assertTrue(Documento.valido("52998224725"))
        assertEquals("52998224725", Documento.exigirValido("529.982.247-25"))
    }

    @Test
    fun `aceita cnpj valido`() {
        assertTrue(Documento.valido("11.222.333/0001-81"))
        assertEquals("11222333000181", Documento.exigirValido("11.222.333/0001-81"))
    }

    @Test
    fun `aceita cnpj alfanumerico, valido desde 2026`() {
        assertTrue(Documento.valido("12.ABC.345/01DE-35"))
        assertEquals("12ABC34501DE35", Documento.exigirValido("12.ABC.345/01DE-35"))
        assertEquals(TipoDeDocumento.CNPJ, Documento.tipoDe("12ABC34501DE35"))
    }

    @Test
    fun `normaliza letra minuscula do cnpj alfanumerico`() {
        assertEquals("12ABC34501DE35", Documento.exigirValido("12.abc.345/01de-35"))
    }

    @Test
    fun `recusa cnpj alfanumerico com digito verificador errado`() {
        assertFalse(Documento.valido("12ABC34501DE34"))
    }

    @Test
    fun `recusa letra nos digitos verificadores do cnpj`() {
        assertFalse(Documento.valido("12ABC34501DEAB"))
    }

    @Test
    fun `recusa letra no cpf`() {
        assertFalse(Documento.valido("5299822472A"))
    }

    @Test
    fun `recusa digito verificador errado`() {
        assertFalse(Documento.valido("52998224724"))
        assertFalse(Documento.valido("11222333000180"))
    }

    @Test
    fun `recusa documento de digitos repetidos`() {
        assertFalse(Documento.valido("11111111111"))
        assertFalse(Documento.valido("00000000000000"))
    }

    @Test
    fun `recusa tamanho que nao e de cpf nem de cnpj`() {
        assertFalse(Documento.valido("123"))
        val erro = assertFailsWith<DocumentoInvalidoException> { Documento.tipoDe("123456") }
        assertTrue(erro.message!!.contains("11 caracteres"))
    }

    @Test
    fun `deriva o tipo pelo tamanho`() {
        assertEquals(TipoDeDocumento.CPF, Documento.tipoDe("529.982.247-25"))
        assertEquals(TipoDeDocumento.CNPJ, Documento.tipoDe("11222333000181"))
    }
}
