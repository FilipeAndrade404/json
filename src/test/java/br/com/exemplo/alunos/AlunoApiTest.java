package br.com.exemplo.alunos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AlunoApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void cadastraEConsultaMesmoAluno() throws Exception {
        var resposta = mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Silva\",\"curso\":\"Ciência da Computação\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse();
        long id = mapper.readTree(resposta.getContentAsByteArray()).get("id").asLong();
        mvc.perform(get("/alunos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.curso").value("Ciência da Computação"));
        mvc.perform(get(resposta.getHeader("Location"))).andExpect(status().isOk());
    }

    @Test
    void retorna404ParaAlunoInexistente() throws Exception {
        mvc.perform(get("/alunos/999999")).andExpect(status().isNotFound());
    }

    @Test
    void rejeitaDadosVaziosEJsonInvalido() throws Exception {
        mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\" \",\"curso\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }
}
