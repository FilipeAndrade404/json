package br.com.exemplo.alunos;

import br.com.exemplo.alunos.proto.AlunoProtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
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
    void comparaCadaAlunoCadastrado(CapturedOutput output) throws Exception {
        for (String nome : new String[]{"João", "Maria"}) {
            var resposta = mvc.perform(post("/alunos").contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsBytes(new CriarAlunoRequest(nome, "Computação"))))
                    .andExpect(status().isCreated()).andReturn().getResponse();
            Aluno aluno = mapper.readValue(resposta.getContentAsByteArray(), Aluno.class);
            Path pasta = Path.of("target", "comparacao", "aluno-" + aluno.id());
            byte[] json = Files.readAllBytes(pasta.resolve("aluno.json"));
            byte[] protobuf = Files.readAllBytes(pasta.resolve("aluno.pb"));
            assertThat(mapper.readValue(json, Aluno.class)).isEqualTo(aluno);
            var mensagem = AlunoProtos.Aluno.parseFrom(protobuf);
            assertThat(new Aluno(mensagem.getId(), mensagem.getNome(), mensagem.getCurso()))
                    .isEqualTo(aluno);
            String relatorio = Files.readString(pasta.resolve("resultado.txt"));
            assertThat(relatorio).contains("ID " + aluno.id(), nome,
                    "JSON: " + json.length + " bytes", "Protobuf: " + protobuf.length + " bytes");
            assertThat(output.getOut()).contains(relatorio);
        }
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
