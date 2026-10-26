# Cadastro de alunos — JSON, REST e Protobuf

Atividade prática em Java 17, Spring Boot, Jackson e Protocol Buffers. O cadastro usa memória: os dados são perdidos ao encerrar a aplicação. O ID é gerado pelo servidor.

## Executar

Use um JDK 17, 21 ou 25, com `JAVA_HOME` configurado. O Maven Wrapper está incluído, dispensando instalar o Maven. No IntelliJ, abra o `pom.xml` como projeto Maven, selecione o JDK e aguarde a importação. A primeira compilação precisa de internet para baixar o Maven, as dependências e o compilador `protoc`; não é necessário instalar o Protobuf separadamente.

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

A API fica em `http://localhost:8080`. Também é possível empacotar e executar:

```shell
.\mvnw.cmd clean package
java -jar target/cadastro-alunos-1.0.0.jar
```

No Linux/macOS, use `sh mvnw` no lugar de `.\mvnw.cmd`. Se já houver Maven instalado, também é possível usar `mvn`.

## Testar a API

| Método e caminho | Resultado |
| --- | --- |
| `POST /alunos` | `201 Created`, aluno em JSON e cabeçalho `Location` |
| `GET /alunos/{id}` | `200 OK` e aluno em JSON; `404 Not Found` se não existir |

O POST recebe `nome` e `curso` obrigatórios. Campos ausentes ou em branco e JSON malformado retornam `400 Bad Request`.

No PowerShell, execute:

```powershell
$dados = @{ nome = 'Ana Silva'; curso = 'Ciência da Computação' } | ConvertTo-Json
$aluno = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/alunos' -ContentType 'application/json; charset=utf-8' -Body ([System.Text.Encoding]::UTF8.GetBytes($dados))
$aluno
Invoke-RestMethod -Uri "http://localhost:8080/alunos/$($aluno.id)"
curl.exe -i http://localhost:8080/alunos/999999
```

Com curl em Bash:

```bash
curl -i -X POST http://localhost:8080/alunos \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana Silva","curso":"Ciência da Computação"}'
curl -i http://localhost:8080/alunos/1
curl -i http://localhost:8080/alunos/999999
```

No Postman, envie POST para `/alunos`, selecione Body → raw → JSON e informe os mesmos campos. Consulte o endereço devolvido no cabeçalho `Location`.

## Serialização e medição

A cada POST válido em `/alunos`, o servidor cadastra o aluno e chama `ComparacaoSerializacao` com os dados recebidos e o ID gerado. Ela serializa e desserializa com Jackson e Protobuf, verifica a igualdade dos dados e imprime no console o aluno, os tamanhos em bytes e a redução percentual. A comparação não é executada na inicialização nem em requisições inválidas.

Por exemplo, para um cadastro que resulte neste aluno:

```json
{"id":1,"nome":"Ana Silva","curso":"Ciência da Computação"}
```

Os arquivos são gravados em `target/comparacao/aluno-{id}/` (por exemplo, `target/comparacao/aluno-1/`):

- `aluno.json`: JSON compacto em UTF-8, sem quebra de linha.
- `aluno.pb`: mensagem binária Protobuf.
- `resultado.txt`: texto JSON gerado, tamanhos e resultado da desserialização.

Para demonstrar no Postman, envie um POST e volte ao console Run do IntelliJ. Aparecerá `Comparação do aluno cadastrado — ID ...`, seguido do JSON e dos tamanhos. Altere o nome ou o curso e envie novamente para ver uma nova comparação. A resposta HTTP continua sendo o aluno em JSON com status 201. Os arquivos de cada ID são sobrescritos se o mesmo ID for reutilizado após reiniciar a aplicação.

| Critério | JSON | Protobuf |
| --- | --- | --- |
| Legibilidade | Texto com nomes dos campos, fácil de inspecionar | Binário; depende de ferramentas e do contrato para interpretação |
| Tamanho neste exemplo | 62 bytes | 39 bytes |
| Esquema | O formato não exige esquema externo; a API ainda precisa de um contrato | O `.proto` define campos, tipos e números usados na geração de código |
| Evolução | Exige cuidado com nomes, tipos e comportamento dos clientes | Exige preservar os números dos campos e reservar os removidos |

Neste exemplo, Protobuf reduz o payload em **37,10%**. A comparação considera somente os bytes dos corpos, sem cabeçalhos HTTP, compressão ou custo de distribuir o esquema. Outros dados produzem outros resultados; esta medição não é um benchmark de velocidade.

O contrato está em `src/main/proto/aluno.proto`. `mvn compile` gera `AlunoProtos.java` em `target/generated-sources/protobuf/java/`. As classes geradas não devem ser editadas manualmente. A API HTTP desta atividade troca JSON; Protobuf é usado na demonstração de serialização binária.

## Reflexão: quando escolher cada formato em uma API REST?

JSON é adequado para APIs públicas, aplicações web e integrações com clientes variados, quando a facilidade de leitura, depuração e uso de ferramentas é relevante. É possível examinar as mensagens diretamente com curl, navegador e Postman, sem gerar classes a partir de um arquivo de esquema.

Protobuf é adequado para comunicação entre serviços e clientes controlados pela mesma organização, especialmente quando há grande volume de mensagens ou restrições de banda. O contrato tipado e a geração de classes ajudam a coordenar a integração, mas exigem distribuir e versionar o `.proto`. Deve-se medir o efeito real nos dados da aplicação antes de decidir.

REST é um estilo de arquitetura e não obriga o uso de JSON. Uma API pode transportar Protobuf sobre HTTP com tipos de mídia e negociação de conteúdo apropriados; isso não a transforma automaticamente em gRPC.

## Código e testes

- `Aluno`: modelo com `id`, `nome` e `curso`.
- `CriarAlunoRequest`: entrada validada do POST.
- `AlunoRepository`: armazenamento em memória com geração de IDs segura para requisições concorrentes.
- `AlunoController`: cadastro e consulta por HTTP.
- `ComparacaoSerializacao`: conversão, leitura e medição dos dois formatos.
- `AlunoApiTest`: criação, consulta, Location, 404, entradas inválidas e comparação de cada aluno cadastrado.
- `SerializacaoTest`: preservação de acentos e tamanhos dos payloads.

Referências: [Spring Boot](https://spring.io/guides/gs/spring-boot) e [guia Java do Protobuf](https://protobuf.dev/getting-started/javatutorial/).
