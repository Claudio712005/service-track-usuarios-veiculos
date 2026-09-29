package com.clau.service_track.usuarios_veiculos.config

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import java.time.Duration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(PropriedadesDeVerificacaoDeCredencial::class)
class ConfiguracaoDeResiliencia {

    @Bean
    fun limitadorDaVerificacaoDeCredencial(propriedades: PropriedadesDeVerificacaoDeCredencial): RateLimiter = RateLimiter.of(
        "verificacaoDeCredencial",
        RateLimiterConfig.custom()
            .limitForPeriod(propriedades.limitePorJanela)
            .limitRefreshPeriod(propriedades.janela)
            .timeoutDuration(Duration.ZERO)
            .build(),
    )
}
