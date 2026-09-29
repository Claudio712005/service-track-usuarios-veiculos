package com.clau.service_track.usuarios_veiculos.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConfiguracaoDoOpenApi {

    @Bean
    fun definicaoDaApi(): OpenAPI = OpenAPI().info(
        Info()
            .title("ServiceTrack — usuários e veículos")
            .version("v1")
            .description(
                "Cadastro de mecânicos e clientes da oficina, e conferência de credencial para a Lambda de " +
                    "autenticação emitir o JWT. Veículos entram em etapa posterior."
            )
    )
}
