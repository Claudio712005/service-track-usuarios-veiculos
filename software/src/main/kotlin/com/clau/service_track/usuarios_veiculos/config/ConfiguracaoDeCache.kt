package com.clau.service_track.usuarios_veiculos.config

import com.clau.service_track.usuarios_veiculos.dto.UsuarioResponse
import java.time.Duration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import tools.jackson.databind.ObjectMapper

@Configuration
@ConditionalOnProperty(name = ["spring.cache.type"], havingValue = "redis")
class ConfiguracaoDeCache {

    @Bean
    fun configuracaoDoCache(mapper: ObjectMapper): RedisCacheConfiguration = RedisCacheConfiguration
        .defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .disableCachingNullValues()
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                JacksonJsonRedisSerializer(mapper, UsuarioResponse::class.java)
            )
        )
}
