package com.clau.service_track.usuarios_veiculos.controller

import com.clau.service_track.usuarios_veiculos.repository.UsuarioRepository
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
class UsuarioControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repositorio: UsuarioRepository

    @BeforeEach
    fun limpar() {
        repositorio.deleteAll()
    }

    @Test
    fun `cadastra cliente e devolve 201 com Location`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(clienteComCnpj()))
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.documento").value("11222333000181"))
            .andExpect(jsonPath("$.tipoDeDocumento").value("CNPJ"))
            .andExpect(jsonPath("$.roles[0]").value("CLIENTE"))
            .andExpect(jsonPath("$.ativo").value(true))
            .andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.senhaHash").doesNotExist())
    }

    @Test
    fun `normaliza o documento e remove a pontuacao`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(mecanico()))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.documento").value("52998224725"))
    }

    @Test
    fun `mecanico com cnpj e recusado`() {
        val corpo = """
            {"documento":"11222333000181","nome":"Oficina Ltda","email":"of@x.com",
             "senha":"senha-de-teste","tipoDeUsuario":"MECANICO"}
        """.trimIndent()
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("DOCUMENTO_INVALIDO"))
            .andExpect(jsonPath("$.mensagem", containsString("pessoa física")))
    }

    @Test
    fun `documento invalido e recusado antes de tocar o banco`() {
        val corpo = """
            {"documento":"11111111111","nome":"Alguem","email":"a@x.com",
             "senha":"senha-de-teste","tipoDeUsuario":"CLIENTE"}
        """.trimIndent()
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("DOCUMENTO_INVALIDO"))
    }

    @Test
    fun `corpo sem campo obrigatorio devolve as violacoes`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content("""{"documento":"52998224725"}"""))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
            .andExpect(jsonPath("$.violacoes").isArray)
    }

    @Test
    fun `documento repetido devolve 409`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(mecanico()))
            .andExpect(status().isCreated)
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(mecanico("outro@x.com")))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.codigo").value("CONFLITO"))
    }

    @Test
    fun `busca por documento aceita pontuacao`() {
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(mecanico()))
            .andExpect(status().isCreated)

        mockMvc.perform(get("$ROTA/por-documento").param("documento", "529.982.247-25"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("Joana Ferreira"))
    }

    @Test
    fun `busca por cnpj alfanumerico pontuado, que tem barra`() {
        val corpo = """
            {"documento":"12.abc.345/01de-35","nome":"Transportes ABC","email":"abc@x.com",
             "senha":"senha-de-teste","tipoDeUsuario":"CLIENTE"}
        """.trimIndent()
        mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.documento").value("12ABC34501DE35"))

        mockMvc.perform(get("$ROTA/por-documento").param("documento", "12.ABC.345/01DE-35"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("Transportes ABC"))
    }

    @Test
    fun `documento sem cadastro devolve 404`() {
        mockMvc.perform(get("$ROTA/por-documento").param("documento", "52998224725"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"))
    }

    @Test
    fun `lista filtra por tipo e ignora desativado`() {
        val id = criar(mecanico())
        criar(clienteComCnpj())

        mockMvc.perform(get(ROTA)).andExpect(jsonPath("$", hasSize<Any>(2)))
        mockMvc.perform(get("$ROTA?tipo=MECANICO")).andExpect(jsonPath("$", hasSize<Any>(1)))

        mockMvc.perform(delete("$ROTA/$id")).andExpect(status().isNoContent)
        mockMvc.perform(get(ROTA)).andExpect(jsonPath("$", hasSize<Any>(1)))
        mockMvc.perform(get("$ROTA/$id")).andExpect(status().isOk)
    }

    @Test
    fun `atualiza contato sem trocar documento`() {
        val id = criar(mecanico())
        val corpo = """{"nome":"Joana Ferreira Lima","email":"joana.lima@x.com","telefone":"11999998888"}"""

        mockMvc.perform(put("$ROTA/$id").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.nome").value("Joana Ferreira Lima"))
            .andExpect(jsonPath("$.telefone").value("11999998888"))
            .andExpect(jsonPath("$.documento").value("52998224725"))
    }

    @Test
    fun `segunda desativacao devolve 409`() {
        val id = criar(mecanico())
        mockMvc.perform(delete("$ROTA/$id")).andExpect(status().isNoContent)
        mockMvc.perform(delete("$ROTA/$id"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.mensagem", containsString("já está desativado")))
    }

    private fun criar(corpo: String): String {
        val resposta = mockMvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString
        return Regex("\"id\":\"([^\"]+)\"").find(resposta)!!.groupValues[1]
    }

    private fun mecanico(email: String = "joana@oficina.com.br") = """
        {"documento":"529.982.247-25","nome":"Joana Ferreira","email":"$email",
         "senha":"senha-de-teste","tipoDeUsuario":"MECANICO","telefone":"11988887777"}
    """.trimIndent()

    private fun clienteComCnpj() = """
        {"documento":"11.222.333/0001-81","nome":"Transportes Silva","email":"contato@silva.com.br",
         "senha":"senha-de-teste","tipoDeUsuario":"CLIENTE"}
    """.trimIndent()

    private companion object {
        const val ROTA = "/usuarios"
    }
}
