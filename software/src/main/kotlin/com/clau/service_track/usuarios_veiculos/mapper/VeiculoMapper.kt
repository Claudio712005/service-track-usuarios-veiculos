package com.clau.service_track.usuarios_veiculos.mapper

import com.clau.service_track.usuarios_veiculos.dto.VeiculoResponse
import com.clau.service_track.usuarios_veiculos.entity.Veiculo
import org.springframework.stereotype.Component

@Component
class VeiculoMapper {

    fun paraResposta(veiculo: Veiculo) = VeiculoResponse(
        id = requireNotNull(veiculo.id),
        clienteId = veiculo.clienteId,
        placa = veiculo.placa,
        marca = veiculo.marca,
        modelo = veiculo.modelo,
        anoModelo = veiculo.anoModelo,
        cor = veiculo.cor,
        chassi = veiculo.chassi,
        ativo = veiculo.ativo,
        dataCriacao = veiculo.dataCriacao.toString(),
        dataAtualizacao = veiculo.dataAtualizacao.toString(),
    )

    fun paraResposta(veiculos: List<Veiculo>) = veiculos.map(::paraResposta)
}
