package br.com.exemplo.alunos;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class AlunoRepository {
    private final ConcurrentHashMap<Long, Aluno> alunos = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();

    public Aluno salvar(CriarAlunoRequest request) {
        long id = sequencia.incrementAndGet();
        Aluno aluno = new Aluno(id, request.nome(), request.curso());
        alunos.put(id, aluno);
        return aluno;
    }

    public Optional<Aluno> buscar(long id) {
        return Optional.ofNullable(alunos.get(id));
    }
}
