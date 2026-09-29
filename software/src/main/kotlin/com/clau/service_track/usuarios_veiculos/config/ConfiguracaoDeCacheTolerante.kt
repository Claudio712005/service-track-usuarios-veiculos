package com.clau.service_track.usuarios_veiculos.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.Cache
import org.springframework.cache.annotation.CachingConfigurer
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["spring.cache.type"], havingValue = "redis")
class ConfiguracaoDeCacheTolerante : CachingConfigurer {

    private val log = LoggerFactory.getLogger(ConfiguracaoDeCacheTolerante::class.java)

    override fun errorHandler(): CacheErrorHandler = object : CacheErrorHandler {

        override fun handleCacheGetError(excecao: RuntimeException, cache: Cache, chave: Any) =
            registrarFalha("leitura", cache, excecao)

        override fun handleCachePutError(excecao: RuntimeException, cache: Cache, chave: Any, valor: Any?) =
            registrarFalha("escrita", cache, excecao)

        override fun handleCacheEvictError(excecao: RuntimeException, cache: Cache, chave: Any) =
            registrarFalha("remocao", cache, excecao)

        override fun handleCacheClearError(excecao: RuntimeException, cache: Cache) =
            registrarFalha("limpeza", cache, excecao)

        private fun registrarFalha(operacao: String, cache: Cache, excecao: RuntimeException) {
            log.warn(
                "cache indisponivel na {} cache={} causa={}",
                operacao, cache.name, excecao.javaClass.simpleName,
            )
        }
    }
}
