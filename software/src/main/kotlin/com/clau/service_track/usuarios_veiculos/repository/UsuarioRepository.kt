package com.clau.service_track.usuarios_veiculos.repository

import com.clau.service_track.usuarios_veiculos.entity.TipoDeUsuario
import com.clau.service_track.usuarios_veiculos.entity.Usuario
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface UsuarioRepository : JpaRepository<Usuario, UUID> {

    fun findByDocumento(documento: String): Usuario?

    fun findByEmailIgnoreCase(email: String): Usuario?

    fun existsByDocumento(documento: String): Boolean

    fun existsByEmailIgnoreCase(email: String): Boolean

    fun findByAtivoTrueOrderByNome(): List<Usuario>

    fun findByTipoDeUsuarioAndAtivoTrueOrderByNome(tipoDeUsuario: TipoDeUsuario): List<Usuario>
}
