package com.clau.service_track.usuarios_veiculos.service

import com.clau.service_track.usuarios_veiculos.dominio.Chassi
import com.clau.service_track.usuarios_veiculos.dominio.Placa
import com.clau.service_track.usuarios_veiculos.dto.AtualizarVeiculoRequest
import com.clau.service_track.usuarios_veiculos.dto.CriarVeiculoRequest
import com.clau.service_track.usuarios_veiculos.entity.TipoDeUsuario
import com.clau.service_track.usuarios_veiculos.entity.Veiculo
import com.clau.service_track.usuarios_veiculos.excecao.ConflitoException
import com.clau.service_track.usuarios_veiculos.excecao.RecursoNaoEncontradoException
import com.clau.service_track.usuarios_veiculos.repository.VeiculoRepository
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VeiculoService(
    private val repositorio: VeiculoRepository,
    private val usuarios: UsuarioService,
) {

    private val log = LoggerFactory.getLogger(VeiculoService::class.java)

    @Transactional
    fun criar(requisicao: CriarVeiculoRequest): Veiculo {
        val placa = Placa.exigirValida(requisicao.placa)
        val chassi = Chassi.exigirValidoSeInformado(requisicao.chassi)
        exigirClienteAtivo(requisicao.clienteId)

        if (repositorio.existsByPlaca(placa)) {
            log.warn("cadastro de veículo recusado: placa já existe placa={}", placa)
            throw ConflitoException("Já existe veículo cadastrado com a placa informada")
        }

        val veiculo = Veiculo(
            clienteId = requisicao.clienteId,
            placa = placa,
            marca = requisicao.marca.trim(),
            modelo = requisicao.modelo.trim(),
            anoModelo = requisicao.anoModelo,
            cor = requisicao.cor?.trim()?.ifBlank { null },
            chassi = chassi,
            ativo = true,
        )

        val salvo = repositorio.save(veiculo)
        log.info(
            "veículo cadastrado id={} placa={} clienteId={} marca={} ano={}",
            salvo.id, salvo.placa, salvo.clienteId, salvo.marca, salvo.anoModelo,
        )
        return salvo
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: UUID): Veiculo = repositorio.findById(id).orElseThrow {
        RecursoNaoEncontradoException("Veículo", id.toString())
    }

    @Transactional(readOnly = true)
    fun buscarPorPlaca(placa: String): Veiculo {
        val normalizada = Placa.normalizar(placa)
        return repositorio.findByPlaca(normalizada)
            ?: throw RecursoNaoEncontradoException("Veículo de placa", normalizada)
    }

    @Transactional(readOnly = true)
    fun listar(clienteId: UUID?): List<Veiculo> = if (clienteId == null) {
        repositorio.findByAtivoTrueOrderByPlaca()
    } else {
        exigirClienteExistente(clienteId)
        repositorio.findByClienteIdAndAtivoTrueOrderByPlaca(clienteId)
    }

    @Transactional
    fun atualizar(id: UUID, requisicao: AtualizarVeiculoRequest): Veiculo {
        val veiculo = buscarPorId(id)

        veiculo.marca = requisicao.marca.trim()
        veiculo.modelo = requisicao.modelo.trim()
        veiculo.anoModelo = requisicao.anoModelo
        veiculo.cor = requisicao.cor?.trim()?.ifBlank { null }
        veiculo.chassi = Chassi.exigirValidoSeInformado(requisicao.chassi)

        val salvo = repositorio.save(veiculo)
        log.info("veículo atualizado id={} placa={}", id, salvo.placa)
        return salvo
    }

    @Transactional
    fun desativar(id: UUID) {
        val veiculo = buscarPorId(id)
        if (!veiculo.ativo) {
            throw ConflitoException("Veículo já está desativado")
        }
        veiculo.ativo = false
        repositorio.save(veiculo)
        log.info("veículo desativado id={} placa={}", id, veiculo.placa)
    }

    private fun exigirClienteAtivo(clienteId: UUID) {
        val cliente = exigirClienteExistente(clienteId)
        if (!cliente.ativo) {
            throw ConflitoException("Cliente está desativado e não recebe veículo novo")
        }
    }

    private fun exigirClienteExistente(clienteId: UUID) = usuarios.buscarPorId(clienteId).also {
        if (it.tipoDeUsuario != TipoDeUsuario.CLIENTE) {
            throw ConflitoException("Veículo pertence a cliente; o usuário informado é ${it.tipoDeUsuario}")
        }
    }
}
