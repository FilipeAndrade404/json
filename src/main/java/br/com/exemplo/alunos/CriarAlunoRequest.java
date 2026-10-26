package br.com.exemplo.alunos;

import jakarta.validation.constraints.NotBlank;

public record CriarAlunoRequest(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "O curso é obrigatório") String curso) {
}
