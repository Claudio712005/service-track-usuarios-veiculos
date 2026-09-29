package com.clau.service_track.usuarios_veiculos.service

import com.clau.service_track.usuarios_veiculos.dominio.Documento
import com.clau.service_track.usuarios_veiculos.dominio.DocumentoInvalidoException
import com.clau.service_track.usuarios_veiculos.dto.AtualizarUsuarioRequest
import com.clau.service_track.usuarios_veiculos.dto.CriarUsuarioRequest
import com.clau.service_track.usuarios_veiculos.dto.UsuarioResponse
import com.clau.service_track.usuarios_veiculos.entity.TipoDeDocumento
import com.clau.service_track.usuarios_veiculos.entity.TipoDeUsuario
import com.clau.service_track.usuarios_veiculos.entity.Usuario
import com.clau.service_track.usuarios_veiculos.excecao.ConflitoException
import com.clau.service_track.usuarios_veiculos.excecao.RecursoNaoEncontradoException
import com.clau.service_track.usuarios_veiculos.mapper.UsuarioMapper
import com.clau.service_track.usuarios_veiculos.repository.UsuarioRepository
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsuarioService(
    private val repositorio: UsuarioRepository,
    private val codificador: PasswordEncoder,
    private val mapper: UsuarioMapper,
) {

    private val log = LoggerFactory.getLogger(UsuarioService::class.java)

    @Transactional
    fun criar(requisicao: CriarUsuarioRequest): Usuario {
        val documento = Documento.exigirValido(requisicao.documento)
        val tipoDeDocumento = Documento.tipoDe(documento)
        exigirDocumentoCompativel(requisicao.tipoDeUsuario, tipoDeDocumento)

        if (repositorio.existsByDocumento(documento)) {
            log.warn("cadastro recusado: documento já existe tipo={}", requisicao.tipoDeUsuario)
            throw ConflitoException("Já existe usuário com o documento informado")
        }
        if (repositorio.existsByEmailIgnoreCase(requisicao.email.trim())) {
            log.warn("cadastro recusado: e-mail já existe tipo={}", requisicao.tipoDeUsuario)
            throw ConflitoException("Já existe usuário com o e-mail informado")
        }

        val usuario = Usuario(
            documento = documento,
            tipoDeDocumento = tipoDeDocumento,
            nome = requisicao.nome.trim(),
            email = requisicao.email.trim(),
            senhaHash = gerarHashDaSenha(requisicao.senha),
            tipoDeUsuario = requisicao.tipoDeUsuario,
            telefone = requisicao.telefone?.trim()?.ifBlank { null },
            ativo = true,
            roles = resolverRoles(requisicao.roles, requisicao.tipoDeUsuario),
        )

        val salvo = repositorio.save(usuario)
        log.info(
            "usuário cadastrado id={} tipo={} tipoDocumento={} roles={}",
            salvo.id, salvo.tipoDeUsuario, salvo.tipoDeDocumento, salvo.roles.size,
        )
        return salvo
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID): Usuario = repositorio.findById(id).orElseThrow {
        RecursoNaoEncontradoException("Usuário", id.toString())
    }

    @Transactional(readOnly = true)
    fun buscarPorDocumento(documento: String): Usuario {
        val digitos = Documento.normalizar(documento)
        return repositorio.findByDocumento(digitos)
            ?: throw RecursoNaoEncontradoException("Usuário de documento", digitos)
    }

    @Cacheable(cacheNames = [CACHE_POR_DOCUMENTO], key = "#documento")
    @Transactional(readOnly = true)
    fun consultarPorDocumento(documento: String): UsuarioResponse = mapper.paraResposta(buscarPorDocumento(documento))

    @Transactional(readOnly = true)
    fun listar(tipoDeUsuario: TipoDeUsuario?): List<Usuario> = if (tipoDeUsuario == null) {
        repositorio.findByAtivoTrueOrderByNome()
    } else {
        repositorio.findByTipoDeUsuarioAndAtivoTrueOrderByNome(tipoDeUsuario)
    }

    @CacheEvict(cacheNames = [CACHE_POR_DOCUMENTO], allEntries = true)
    @Transactional
    fun atualizar(id: UUID, requisicao: AtualizarUsuarioRequest): Usuario {
        val usuario = buscarPorId(id)

        repositorio.findByEmailIgnoreCase(requisicao.email.trim())
            ?.takeIf { it.id != id }
            ?.let { throw ConflitoException("E-mail informado já pertence a outro usuário") }

        usuario.nome = requisicao.nome.trim()
        usuario.email = requisicao.email.trim()
        usuario.telefone = requisicao.telefone?.trim()?.ifBlank { null }
        requisicao.senha?.takeIf { it.isNotBlank() }?.let { usuario.senhaHash = gerarHashDaSenha(it) }
        requisicao.roles?.takeIf { it.isNotEmpty() }?.let { usuario.roles = resolverRoles(it, usuario.tipoDeUsuario) }

        val salvo = repositorio.save(usuario)
        log.info("usuário atualizado id={} senhaTrocada={}", id, requisicao.senha != null)
        return salvo
    }

    @CacheEvict(cacheNames = [CACHE_POR_DOCUMENTO], allEntries = true)
    @Transactional
    fun desativar(id: UUID) {
        val usuario = buscarPorId(id)
        if (!usuario.ativo) {
            throw ConflitoException("Usuário já está desativado")
        }
        usuario.ativo = false
        repositorio.save(usuario)
        log.info("usuário desativado id={}", id)
    }

    private fun gerarHashDaSenha(senha: String): String =
        requireNotNull(codificador.encode(senha)) { "codificador de senha devolveu nulo" }

    private fun exigirDocumentoCompativel(tipoDeUsuario: TipoDeUsuario, tipoDeDocumento: TipoDeDocumento) {
        if (tipoDeUsuario == TipoDeUsuario.MECANICO && tipoDeDocumento != TipoDeDocumento.CPF) {
            throw DocumentoInvalidoException("Mecânico é pessoa física: informe um CPF")
        }
    }

    private fun resolverRoles(informados: Set<String>?, tipoDeUsuario: TipoDeUsuario): MutableSet<String> =
        if (informados.isNullOrEmpty()) {
            mutableSetOf(tipoDeUsuario.name)
        } else {
            informados.map { it.trim().uppercase() }.toMutableSet()
        }

    companion object {
        const val CACHE_POR_DOCUMENTO = "usuarioPorDocumento"
    }
}
