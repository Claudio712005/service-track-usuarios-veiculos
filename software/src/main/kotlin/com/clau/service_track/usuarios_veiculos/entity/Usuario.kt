package com.clau.service_track.usuarios_veiculos.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Entity
@Table(name = "USUARIOS")
class Usuario(

    @Id
    @Column(name = "ID", nullable = false)
    var id: UUID? = null,

    @Column(name = "DOCUMENTO", nullable = false, unique = true, length = 14)
    var documento: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_DOCUMENTO", nullable = false, length = 4)
    var tipoDeDocumento: TipoDeDocumento,

    @Column(name = "NOME", nullable = false, length = 120)
    var nome: String,

    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    var email: String,

    @Column(name = "SENHA_HASH", nullable = false, length = 72)
    var senhaHash: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_USUARIO", nullable = false, length = 10)
    var tipoDeUsuario: TipoDeUsuario,

    @Column(name = "TELEFONE", length = 20)
    var telefone: String? = null,

    @Column(name = "ATIVO", nullable = false)
    var ativo: Boolean = true,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "USUARIO_ROLES", joinColumns = [JoinColumn(name = "USUARIO_ID")])
    @Column(name = "ROLE", nullable = false, length = 20)
    var roles: MutableSet<String> = mutableSetOf(),

    @Column(name = "DATA_CRIACAO", nullable = false)
    var dataCriacao: OffsetDateTime? = null,

    @Column(name = "DATA_ATUALIZACAO", nullable = false)
    var dataAtualizacao: OffsetDateTime? = null,
) {

    @PrePersist
    fun aoCriar() {
        val agora = OffsetDateTime.now(ZoneOffset.UTC)
        if (id == null) id = UUID.randomUUID()
        dataCriacao = agora
        dataAtualizacao = agora
    }

    @PreUpdate
    fun aoAtualizar() {
        dataAtualizacao = OffsetDateTime.now(ZoneOffset.UTC)
    }
}
