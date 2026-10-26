package br.com.exemplo.alunos;

import br.com.exemplo.alunos.proto.AlunoProtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SerializacaoTest {
    @Test
    void preservaDadosComAcentosNosDoisFormatos() throws Exception {
        Aluno aluno = new Aluno(1, "Ana Silva", "Ciência da Computação");
        ObjectMapper mapper = new ObjectMapper();
        byte[] json = mapper.writeValueAsBytes(aluno);
        assertThat(mapper.readValue(json, Aluno.class)).isEqualTo(aluno);
        byte[] protobuf = AlunoProtos.Aluno.newBuilder().setId(aluno.id())
                .setNome(aluno.nome()).setCurso(aluno.curso()).build().toByteArray();
        var recuperado = AlunoProtos.Aluno.parseFrom(protobuf);
        assertThat(new Aluno(recuperado.getId(), recuperado.getNome(), recuperado.getCurso()))
                .isEqualTo(aluno);
        assertThat(json).hasSize(62);
        assertThat(protobuf).hasSize(39);
    }
}
