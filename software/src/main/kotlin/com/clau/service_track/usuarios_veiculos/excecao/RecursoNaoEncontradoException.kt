package com.clau.service_track.usuarios_veiculos.excecao

class RecursoNaoEncontradoException(recurso: String, identificador: String) :
    RuntimeException("$recurso '$identificador' não encontrado")
