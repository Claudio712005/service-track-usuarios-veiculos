package com.clau.service_track.usuarios_veiculos.service

import com.clau.service_track.usuarios_veiculos.dominio.Documento
import com.clau.service_track.usuarios_veiculos.entity.Usuario
import com.clau.service_track.usuarios_veiculos.excecao.CredencialInvalidaException
import com.clau.service_track.usuarios_veiculos.repository.UsuarioRepository
import io.github.resilience4j.ratelimiter.RateLimiter
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class VerificacaoDeCredencialService(
    private val repositorio: UsuarioRepository,
    private val codificador: PasswordEncoder,
    private val limitador: RateLimiter,
) {

    private val log = LoggerFactory.getLogger(VerificacaoDeCredencialService::class.java)

    fun verificar(documentoBruto: String, senha: String): Usuario =
        RateLimiter.decorateSupplier(limitador) { conferirCredencial(documentoBruto, senha) }.get()

    private fun conferirCredencial(documentoBruto: String, senha: String): Usuario {
        val documento = Documento.normalizar(documentoBruto)
        val usuario = repositorio.findByDocumento(documento)

        if (usuario == null) {
            codificador.matches(senha, HASH_PARA_TEMPO_CONSTANTE)
            log.warn("verificação recusada: documento sem cadastro")
            throw CredencialInvalidaException()
        }
        if (!usuario.ativo) {
            log.warn("verificação recusada: usuário desativado id={}", usuario.id)
            throw CredencialInvalidaException()
        }
        if (!codificador.matches(senha, usuario.senhaHash)) {
            log.warn("verificação recusada: senha incorreta id={}", usuario.id)
            throw CredencialInvalidaException()
        }

        log.info("credencial verificada id={} tipo={}", usuario.id, usuario.tipoDeUsuario)
        return usuario
    }

    private companion object {
        const val HASH_PARA_TEMPO_CONSTANTE = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
    }
}
