package com.clau.service_track.usuarios_veiculos.dominio

object Mascara {

    private const val CARACTERES_VISIVEIS = 4

    fun documento(bruto: String?): String {
        val limpo = Documento.normalizar(bruto)
        if (limpo.length <= CARACTERES_VISIVEIS) return "***"
        return "***" + limpo.takeLast(CARACTERES_VISIVEIS)
    }
}
