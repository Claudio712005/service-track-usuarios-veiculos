package com.clau.service_track.usuarios_veiculos.controller

import com.clau.service_track.usuarios_veiculos.repository.UsuarioRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class CredencialControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var repositorio: UsuarioRepository

    @BeforeEach
    fun preparar() {
        repositorio.deleteAll()
        mockMvc.perform(
            post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(
                """
                {"documento":"529.982.247-25","nome":"Joana Ferreira","email":"joana@oficina.com.br",
                 "senha":"senha-de-teste","tipoDeUsuario":"MECANICO"}
                """.trimIndent()
            )
        ).andExpect(status().isCreated)
    }

    @Test
    fun `credencial correta devolve identidade e resolverPapeis`() {
        verificar("529.982.247-25", "senha-de-teste")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.documento").value("52998224725"))
            .andExpect(jsonPath("$.tipoDeUsuario").value("MECANICO"))
            .andExpect(jsonPath("$.roles[0]").value("MECANICO"))
    }

    @Test
    fun `senha errada devolve 401 sem dizer que o documento existe`() {
        verificar("52998224725", "senha-errada")
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.codigo").value("CREDENCIAL_INVALIDA"))
            .andExpect(jsonPath("$.mensagem").value("Documento ou senha inválidos"))
    }

    @Test
    fun `documento sem cadastro devolve o mesmo 401 da senha errada`() {
        verificar("11222333000181", "senha-de-teste")
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.mensagem").value("Documento ou senha inválidos"))
    }

    @Test
    fun `usuario desativado nao autentica`() {
        val id = repositorio.findByDocumento("52998224725")!!.id
        mockMvc.perform(delete("/usuarios/$id")).andExpect(status().isNoContent)

        verificar("52998224725", "senha-de-teste").andExpect(status().isUnauthorized)
    }

    @Test
    fun `corpo sem senha e recusado na borda`() {
        mockMvc.perform(
            post(ROTA).contentType(MediaType.APPLICATION_JSON).content("""{"documento":"52998224725"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
    }

    private fun verificar(documento: String, senha: String) = mockMvc.perform(
        post(ROTA).contentType(MediaType.APPLICATION_JSON)
            .content("""{"documento":"$documento","senha":"$senha"}""")
    )

    private companion object {
        const val ROTA = "/credenciais/verificacao"
    }
}
