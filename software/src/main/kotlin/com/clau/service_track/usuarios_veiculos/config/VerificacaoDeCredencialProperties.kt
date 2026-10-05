package com.clau.service_track.usuarios_veiculos.config

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "servicetrack.verificacao-de-credencial")
data class VerificacaoDeCredencialProperties(

    val limitePorJanela: Int = 20,

    val janela: Duration = Duration.ofSeconds(1),
)
