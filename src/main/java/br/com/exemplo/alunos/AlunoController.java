package br.com.exemplo.alunos;

import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(value = "/alunos", produces = MediaType.APPLICATION_JSON_VALUE)
public class AlunoController {
    private final AlunoRepository repository;
    private final ComparacaoSerializacao comparacao;

    public AlunoController(AlunoRepository repository, ComparacaoSerializacao comparacao) {
        this.repository = repository;
        this.comparacao = comparacao;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Aluno> cadastrar(@Valid @RequestBody CriarAlunoRequest request) throws IOException {
        Aluno aluno = repository.salvar(request);
        comparacao.comparar(aluno);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(aluno.id()).toUri();
        return ResponseEntity.created(location).body(aluno);
    }

    @GetMapping("/{id}")
    public Aluno buscar(@PathVariable long id) {
        return repository.buscar(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
    }
}
