package com.clau.service_track.usuarios_veiculos.dominio

import com.clau.service_track.usuarios_veiculos.entity.TipoDeDocumento

object Documento {

    private const val TAMANHO_DO_CPF = 11
    private const val TAMANHO_DO_CNPJ = 14
    private const val POSICOES_DA_BASE_DO_CNPJ = 12
    private const val DESLOCAMENTO_ASCII = 48

    private val PESOS_DO_PRIMEIRO_DIGITO = intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    private val PESOS_DO_SEGUNDO_DIGITO = intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
    private val FORMATO_DO_CPF = Regex("^\\d{11}$")
    private val FORMATO_DO_CNPJ = Regex("^[A-Z0-9]{12}\\d{2}$")

    fun normalizar(bruto: String?): String =
        bruto?.uppercase()?.filter { it.isDigit() || it in 'A'..'Z' } ?: ""

    fun tipoDe(bruto: String?): TipoDeDocumento = when (normalizar(bruto).length) {
        TAMANHO_DO_CPF -> TipoDeDocumento.CPF
        TAMANHO_DO_CNPJ -> TipoDeDocumento.CNPJ
        else -> throw DocumentoInvalidoException(
            "Documento deve ter 11 caracteres (CPF) ou 14 (CNPJ, que desde 2026 aceita letras nas 12 primeiras posições)"
        )
    }

    fun valido(bruto: String?): Boolean {
        val documento = normalizar(bruto)
        return when {
            documento.matches(FORMATO_DO_CPF) -> cpfValido(documento)
            documento.matches(FORMATO_DO_CNPJ) -> cnpjValido(documento)
            else -> false
        }
    }

    fun exigirValido(bruto: String?): String {
        val documento = normalizar(bruto)
        if (!valido(documento)) {
            throw DocumentoInvalidoException(
                "Documento ${Mascara.documento(bruto)} não é um CPF nem um CNPJ válido"
            )
        }
        return documento
    }

    private fun cpfValido(cpf: String): Boolean {
        if (cpf.toSet().size == 1) return false
        val primeiro = digitoDeCpf(cpf, 9, 10)
        val segundo = digitoDeCpf(cpf, 10, 11)
        return primeiro == cpf[9].digitToInt() && segundo == cpf[10].digitToInt()
    }

    private fun digitoDeCpf(cpf: String, tamanho: Int, pesoInicial: Int): Int =
        digitoVerificador((0 until tamanho).sumOf { cpf[it].digitToInt() * (pesoInicial - it) })

    private fun cnpjValido(cnpj: String): Boolean {
        val base = cnpj.take(POSICOES_DA_BASE_DO_CNPJ)
        if (base.all { it == '0' }) return false
        if (base.all { it.isDigit() } && base.toSet().size == 1) return false

        val primeiro = digitoDeCnpj(base, PESOS_DO_PRIMEIRO_DIGITO)
        val segundo = digitoDeCnpj(base + primeiro, PESOS_DO_SEGUNDO_DIGITO)
        return primeiro == cnpj[12].digitToInt() && segundo == cnpj[13].digitToInt()
    }

    private fun digitoDeCnpj(base: String, pesos: IntArray): Int =
        digitoVerificador(pesos.indices.sumOf { valorDoCaractere(base[it]) * pesos[it] })

    private fun valorDoCaractere(caractere: Char): Int = caractere.code - DESLOCAMENTO_ASCII

    private fun digitoVerificador(soma: Int): Int {
        val resto = soma % 11
        return if (resto < 2) 0 else 11 - resto
    }
}
