package com.clau.service_track.usuarios_veiculos.dominio

object Placa {

    private val FORMATO_ANTIGO = Regex("^[A-Z]{3}[0-9]{4}$")
    private val FORMATO_MERCOSUL = Regex("^[A-Z]{3}[0-9][A-Z][0-9]{2}$")

    fun normalizar(bruta: String?): String =
        bruta?.uppercase()?.filter { it.isDigit() || it in 'A'..'Z' } ?: ""

    fun valida(bruta: String?): Boolean {
        val placa = normalizar(bruta)
        return placa.matches(FORMATO_ANTIGO) || placa.matches(FORMATO_MERCOSUL)
    }

    fun exigirValida(bruta: String?): String {
        val placa = normalizar(bruta)
        if (!valida(placa)) {
            throw PlacaInvalidaException(
                "Placa '$bruta' inválida. Use o formato antigo ABC1234 ou o Mercosul ABC1D23"
            )
        }
        return placa
    }
}
