package com.clau.service_track.usuarios_veiculos.dominio

object Chassi {

    private val FORMATO = Regex("^[A-HJ-NPR-Z0-9]{17}$")

    fun normalizar(bruto: String?): String? =
        bruto?.uppercase()?.filter { it.isDigit() || it in 'A'..'Z' }?.ifBlank { null }

    fun exigirValidoSeInformado(bruto: String?): String? {
        val chassi = normalizar(bruto) ?: return null
        if (!chassi.matches(FORMATO)) {
            throw ChassiInvalidoException(
                "Chassi '$bruto' inválido. São 17 posições, sem as letras I, O e Q"
            )
        }
        return chassi
    }
}
