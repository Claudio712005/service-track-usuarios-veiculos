package com.clau.service_track.usuarios_veiculos.repository

import com.clau.service_track.usuarios_veiculos.entity.Veiculo
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface VeiculoRepository : JpaRepository<Veiculo, UUID> {

    fun findByPlaca(placa: String): Veiculo?

    fun existsByPlaca(placa: String): Boolean

    fun findByAtivoTrueOrderByPlaca(): List<Veiculo>

    fun findByClienteIdAndAtivoTrueOrderByPlaca(clienteId: UUID): List<Veiculo>

    fun countByClienteIdAndAtivoTrue(clienteId: UUID): Long
}
