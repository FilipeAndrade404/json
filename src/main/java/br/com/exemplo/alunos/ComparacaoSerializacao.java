package br.com.exemplo.alunos;

import br.com.exemplo.alunos.proto.AlunoProtos;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ComparacaoSerializacao implements CommandLineRunner {
    private final ObjectMapper mapper;

    public ComparacaoSerializacao(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void run(String... args) throws Exception {
        Aluno original = new Aluno(1, "Ana Silva", "Ciência da Computação");
        byte[] json = mapper.writeValueAsBytes(original);
        Aluno recuperadoJson = mapper.readValue(json, Aluno.class);

        AlunoProtos.Aluno mensagem = AlunoProtos.Aluno.newBuilder()
                .setId(original.id()).setNome(original.nome()).setCurso(original.curso()).build();
        byte[] protobuf = mensagem.toByteArray();
        AlunoProtos.Aluno recuperadoProto = AlunoProtos.Aluno.parseFrom(protobuf);
        Aluno recuperadoProtobuf = new Aluno(recuperadoProto.getId(),
                recuperadoProto.getNome(), recuperadoProto.getCurso());
        if (!original.equals(recuperadoJson) || !original.equals(recuperadoProtobuf)) {
            throw new IllegalStateException("Os dados não foram preservados na desserialização");
        }

        String textoJson = new String(json, StandardCharsets.UTF_8);
        double reducao = 100.0 * (json.length - protobuf.length) / json.length;
        String relatorio = String.format(Locale.ROOT,
                "JSON (UTF-8): %s%nJSON: %d bytes%nProtobuf: %d bytes%n"
                + "Redução neste exemplo: %.2f%%%n"
                + "Desserialização JSON e Protobuf: dados preservados.%n",
                textoJson, json.length, protobuf.length, reducao);
        Path pasta = Path.of("target", "comparacao");
        Files.createDirectories(pasta);
        Files.write(pasta.resolve("aluno.json"), json);
        Files.write(pasta.resolve("aluno.pb"), protobuf);
        Files.writeString(pasta.resolve("resultado.txt"), relatorio, StandardCharsets.UTF_8);
        System.out.println(relatorio);
    }
}
