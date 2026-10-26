# Lembretes para a apresentação

## Clean, compile e execução

Execute no terminal do IntelliJ, dentro da pasta do projeto:

```powershell
# Pare a aplicação antes de limpar.
.\mvnw.cmd clean

# Recria as classes geradas pelo Protobuf e compila o projeto.
.\mvnw.cmd compile

# Inicia a API (mantenha esse terminal aberto).
.\mvnw.cmd spring-boot:run
```

- `clean` apaga **toda a pasta target**, incluindo arquivos de comparação e classes geradas. Não apaga o código em `src`.
- Após `clean`, use `compile` antes de executar pelo botão ▶ do IntelliJ. `spring-boot:run` também executa as etapas de compilação necessárias.
- Se aparecer `package br.com.exemplo.alunos.proto does not exist`, execute `compile`. Se o editor continuar vermelho, use **Maven → Reload All Maven Projects**.
- Use `mvnw.cmd`, pois `mvn` pode não estar disponível no terminal.
- Se precisar configurar o Java neste computador, antes dos comandos execute:

```powershell
$env:JAVA_HOME = 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr'
```

## Roteiro no Postman

1. Inicie a aplicação e espere aparecer `Started CadastroAlunosApplication`.
2. Envie **POST** para `http://localhost:8080/alunos`, com **Body → raw → JSON**:

```json
{
  "nome": "Ana Silva",
  "curso": "Ciência da Computação"
}
```

3. Mostre **201 Created**, o ID gerado e o cabeçalho `Location` da resposta.
4. Volte ao console do IntelliJ: cada POST válido imprime o JSON, os tamanhos JSON e Protobuf e a redução percentual.
5. Faça **GET** `http://localhost:8080/alunos/1`, usando o ID retornado no POST e **Body → none**. Mostre **200 OK**.
6. Faça **GET** `http://localhost:8080/alunos/999999`. Mostre **404 Not Found**.
7. Opcional: envie POST com nome e curso vazios para mostrar **400 Bad Request**.

Se aparecer `ECONNREFUSED`, verifique se a aplicação está rodando na porta 8080. O caminho da consulta é `/alunos/1`, e não apenas `/1`.

## O que explicar no código

| Arquivo | Responsabilidade |
| --- | --- |
| `CadastroAlunosApplication` | Inicia o Spring Boot e o servidor. |
| `AlunoController` | Recebe POST/GET e define as respostas HTTP. |
| `CriarAlunoRequest` | Representa nome e curso recebidos e define a validação. |
| `AlunoRepository` | Gera IDs e guarda os alunos em memória. |
| `Aluno` | Representa id, nome e curso. |
| `ComparacaoSerializacao` | Serializa, desserializa, verifica os dados e compara os bytes. |
| `aluno.proto` | Define o contrato Protobuf; não guarda cadastros. |

Em `ComparacaoSerializacao`, mostre `writeValueAsBytes()` e `readValue()` para JSON; `toByteArray()` e `parseFrom()` para Protobuf; `json.length` e `protobuf.length` para os tamanhos.

## Pontos importantes

- **Postman é o cliente; a aplicação Java é a API REST.** A API troca JSON. O Protobuf é usado internamente para comparar o mesmo aluno.
- JSON facilita leitura e depuração. Protobuf exige um esquema e pode reduzir o tamanho das mensagens. A redução depende dos dados; não é uma medição de velocidade.
- Para Ana Silva, Ciência da Computação e ID 1: JSON **62 bytes**, Protobuf **39 bytes**, redução **37,10%**.
- O cadastro em memória é perdido ao encerrar a aplicação. Os arquivos em `target/comparacao/aluno-{id}/` continuam no disco, mas não são carregados pelo GET.
- Ao reiniciar, os IDs recomeçam em 1; arquivos de um ID reutilizado são sobrescritos. Não existe limite de três alunos.
- O `.proto` gera `AlunoProtos.java` em `target/generated-sources/protobuf/java/`. Não edite a classe gerada manualmente.
- Para verificar os testes: `.\mvnw.cmd test` (eles também criam arquivos de comparação).
- Antes da apresentação, teste tudo e deixe as dependências baixadas. Execute a aplicação em apenas um lugar para evitar conflito na porta 8080.
