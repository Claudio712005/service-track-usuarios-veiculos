package com.clau.service_track.usuarios_veiculos.mapper

import com.clau.service_track.usuarios_veiculos.dto.UsuarioResponse
import com.clau.service_track.usuarios_veiculos.dto.VerificacaoDeCredencialResponse
import com.clau.service_track.usuarios_veiculos.entity.Usuario
import org.springframework.stereotype.Component

@Component
class UsuarioMapper {

    fun paraResposta(usuario: Usuario) = UsuarioResponse(
        id = requireNotNull(usuario.id),
        documento = usuario.documento,
        tipoDeDocumento = usuario.tipoDeDocumento,
        nome = usuario.nome,
        email = usuario.email,
        tipoDeUsuario = usuario.tipoDeUsuario,
        telefone = usuario.telefone,
        ativo = usuario.ativo,
        roles = usuario.roles.toSet(),
        dataCriacao = usuario.dataCriacao.toString(),
        dataAtualizacao = usuario.dataAtualizacao.toString(),
    )

    fun paraResposta(usuarios: List<Usuario>) = usuarios.map(::paraResposta)

    fun paraVerificacao(usuario: Usuario) = VerificacaoDeCredencialResponse(
        id = requireNotNull(usuario.id),
        documento = usuario.documento,
        nome = usuario.nome,
        tipoDeUsuario = usuario.tipoDeUsuario,
        roles = usuario.roles.toSet(),
    )
}
