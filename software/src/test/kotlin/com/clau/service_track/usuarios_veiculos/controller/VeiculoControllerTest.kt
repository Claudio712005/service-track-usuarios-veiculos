package com.clau.service_track.usuarios_veiculos.controller

import com.clau.service_track.usuarios_veiculos.repository.UsuarioRepository
import com.clau.service_track.usuarios_veiculos.repository.VeiculoRepository
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class VeiculoControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var veiculos: VeiculoRepository

    @Autowired
    private lateinit var usuarios: UsuarioRepository

    private lateinit var clienteId: String
    private lateinit var mecanicoId: String

    @BeforeEach
    fun preparar() {
        veiculos.deleteAll()
        usuarios.deleteAll()
        clienteId = criarUsuario(
            """
            {"documento":"11.222.333/0001-81","nome":"Transportes Silva","email":"silva@x.com",
             "senha":"senha-de-teste","tipoDeUsuario":"CLIENTE"}
            """.trimIndent()
        )
        mecanicoId = criarUsuario(
            """
            {"documento":"529.982.247-25","nome":"Joana","email":"joana@x.com",
             "senha":"senha-de-teste","tipoDeUsuario":"MECANICO"}
            """.trimIndent()
        )
    }

    @Test
    fun `cadastra veiculo mercosul e normaliza a placa`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(veiculo("rio-2a18")))
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.placa").value("RIO2A18"))
            .andExpect(jsonPath("$.clienteId").value(clienteId))
            .andExpect(jsonPath("$.ativo").value(true))
    }

    @Test
    fun `aceita placa do formato antigo`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(veiculo("ABC-1234")))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.placa").value("ABC1234"))
    }

    @Test
    fun `placa fora dos formatos e recusada`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(veiculo("AB12345")))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("PLACA_INVALIDA"))
    }

    @Test
    fun `chassi com letra proibida e recusado`() {
        val corpo = """
            {"clienteId":"$clienteId","placa":"RIO2A18","marca":"VW","modelo":"Gol",
             "anoModelo":2021,"chassi":"9BWZZZ377VT00425I"}
        """.trimIndent()
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("CHASSI_INVALIDO"))
    }

    @Test
    fun `placa repetida devolve 409`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(veiculo("RIO2A18")))
            .andExpect(status().isCreated)
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(veiculo("rio2a18")))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.codigo").value("CONFLITO"))
    }

    @Test
    fun `veiculo de mecanico e recusado`() {
        val corpo = """
            {"clienteId":"$mecanicoId","placa":"RIO2A18","marca":"VW","modelo":"Gol","anoModelo":2021}
        """.trimIndent()
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.mensagem", containsString("MECANICO")))
    }

    @Test
    fun `cliente inexistente devolve 404`() {
        val corpo = """
            {"clienteId":"0f6f6a9c-1f1e-4a6a-9f8e-2b7c5d4e3a21","placa":"RIO2A18","marca":"VW",
             "modelo":"Gol","anoModelo":2021}
        """.trimIndent()
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `cliente desativado nao recebe veiculo novo`() {
        mockMvc.perform(delete("/usuarios/$clienteId")).andExpect(status().isNoContent)
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(veiculo("RIO2A18")))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.mensagem", containsString("desativado")))
    }

    @Test
    fun `lista filtra pela frota do cliente e ignora desativado`() {
        val id = criar(veiculo("RIO2A18"))
        criar(veiculo("ABC1234"))

        mockMvc.perform(get(ROTA)).andExpect(jsonPath("$", hasSize<Any>(2)))
        mockMvc.perform(get(ROTA).param("clienteId", clienteId)).andExpect(jsonPath("$", hasSize<Any>(2)))

        mockMvc.perform(delete("$ROTA/$id")).andExpect(status().isNoContent)
        mockMvc.perform(get(ROTA)).andExpect(jsonPath("$", hasSize<Any>(1)))
    }

    @Test
    fun `busca por placa aceita hifen e minuscula`() {
        criar(veiculo("RIO2A18"))
        mockMvc.perform(get("$ROTA/por-placa").param("placa", "rio-2a18"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.modelo").value("Gol 1.6"))
    }

    @Test
    fun `placa sem cadastro devolve 404`() {
        mockMvc.perform(get("$ROTA/por-placa").param("placa", "XYZ9876"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"))
    }

    @Test
    fun `atualiza dados sem mexer na placa`() {
        val id = criar(veiculo("RIO2A18"))
        val corpo = """{"marca":"Volkswagen","modelo":"Gol 1.6 Highline","anoModelo":2022,"cor":"Preto"}"""

        mockMvc.perform(put("$ROTA/$id").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.modelo").value("Gol 1.6 Highline"))
            .andExpect(jsonPath("$.anoModelo").value(2022))
            .andExpect(jsonPath("$.placa").value("RIO2A18"))
    }

    @Test
    fun `segunda desativacao devolve 409`() {
        val id = criar(veiculo("RIO2A18"))
        mockMvc.perform(delete("$ROTA/$id")).andExpect(status().isNoContent)
        mockMvc.perform(delete("$ROTA/$id")).andExpect(status().isConflict)
    }

    private fun criarUsuario(corpo: String): String = extrairId(
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
    )

    private fun criar(corpo: String): String = extrairId(
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
    )

    private fun extrairId(resposta: String) = Regex("\"id\":\"([^\"]+)\"").find(resposta)!!.groupValues[1]

    private fun veiculo(placa: String) = """
        {"clienteId":"$clienteId","placa":"$placa","marca":"VW","modelo":"Gol 1.6","anoModelo":2021,"cor":"Prata"}
    """.trimIndent()

    private companion object {
        const val ROTA = "/veiculos"
    }
}
