package com.clau.service_track.usuarios_veiculos.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ConfiguracaoDeSenha {

    @Bean
    fun codificadorDeSenha(): PasswordEncoder = BCryptPasswordEncoder(10)
}
