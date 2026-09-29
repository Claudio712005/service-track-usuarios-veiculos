package com.clau.service_track.usuarios_veiculos.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Entity
@Table(name = "VEICULOS")
class Veiculo(

    @Id
    @Column(name = "ID", nullable = false)
    var id: UUID? = null,

    @Column(name = "CLIENTE_ID", nullable = false)
    var clienteId: UUID,

    @Column(name = "PLACA", nullable = false, unique = true, length = 7)
    var placa: String,

    @Column(name = "MARCA", nullable = false, length = 40)
    var marca: String,

    @Column(name = "MODELO", nullable = false, length = 60)
    var modelo: String,

    @Column(name = "ANO_MODELO", nullable = false)
    var anoModelo: Int,

    @Column(name = "COR", length = 30)
    var cor: String? = null,

    @Column(name = "CHASSI", length = 17)
    var chassi: String? = null,

    @Column(name = "ATIVO", nullable = false)
    var ativo: Boolean = true,

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
