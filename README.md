# stride-backend

API do Stride — controle financeiro pessoal. Spring Boot 4.1 / Java 25 / PostgreSQL 18.

---

## ⚠️ Antes de rodar pela primeira vez

Alguns arquivos **não estão no Git de propósito** (são segredos ou configuração de
máquina). Depois de clonar, você precisa criá-los na mão. Sem eles a aplicação
**não sobe**.

### 1. Chaves de assinatura do JWT (obrigatório)

A API assina os tokens com uma chave privada RSA. Quem tiver essa chave consegue
emitir tokens válidos como se fosse a API — por isso ela nunca entra no
repositório, e **cada pessoa gera o próprio par local**.

```bash
mkdir -p src/main/resources/jwt

# chave privada (PKCS#8) — assina os tokens, é o segredo
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
  -out src/main/resources/jwt/private.pem

# chave pública (X.509) — verifica os tokens, derivada da privada
openssl rsa -pubout \
  -in src/main/resources/jwt/private.pem \
  -out src/main/resources/jwt/public.pem
```

Confira que os cabeçalhos saíram assim — o formato importa, e o Spring recusa
outros:

```bash
head -1 src/main/resources/jwt/private.pem   # -----BEGIN PRIVATE KEY-----
head -1 src/main/resources/jwt/public.pem    # -----BEGIN PUBLIC KEY-----
```

Se aparecer `-----BEGIN RSA PRIVATE KEY-----`, é o formato antigo (PKCS#1) e vai
falhar. Converta com:

```bash
openssl pkcs8 -topk8 -nocrypt -in private.pem -out private-novo.pem
```

> **Como saber que esqueci deste passo:** a aplicação não sobe. Não existe um
> aviso amigável — vem uma pilha longa de `UnsatisfiedDependencyException`
> terminando assim:
>
> ```
> Error creating bean with name 'rsaPublicKey' ... Factory method 'rsaPublicKey'
> threw exception with message: class path resource [jwt/public.pem]
> cannot be opened because it does not exist
> ```
>
> Se ler `[jwt/public.pem] cannot be opened`, é este passo que faltou.

Os tokens que você gerar são válidos só na sua máquina: como cada dev tem um par
diferente, um token gerado no seu ambiente não funciona no de outra pessoa. Isso é
esperado.

### 2. Configuração local (opcional)

Se precisar sobrescrever alguma propriedade só na sua máquina, crie
`src/main/resources/application-local.properties` (também ignorado pelo Git) e rode
com o perfil `local`.

---

## Pré-requisitos

| | |
|---|---|
| **Java 25** | `java -version` |
| **Docker** | precisa estar rodando; o banco sobe sozinho |
| **Maven** | não precisa instalar, use o `./mvnw` do projeto |

No Linux/macOS, dê permissão de execução ao wrapper na primeira vez:
`chmod +x mvnw`

---

## Rodando

```bash
./mvnw spring-boot:run
```

Só isso. O `spring-boot-docker-compose` lê o `compose.yaml`, sobe o PostgreSQL,
espera ficar saudável e injeta a URL de conexão — você **não** configura banco em
lugar nenhum. Em seguida o Flyway aplica as migrations e o seed de categorias.

A API sobe em `http://localhost:8080`.

| | |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| Health | http://localhost:8080/actuator/health |

Para parar: `Ctrl+C`. O container do banco para junto, mas os dados ficam. Para
apagar o banco e começar do zero: `docker compose down -v`.

---

## Primeiro uso da API

```bash
# 1. criar conta
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Seu Nome","email":"voce@stride.dev","password":"senhaForte123"}'

# 2. pegar o token
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"voce@stride.dev","password":"senhaForte123"}'

# 3. usar o token nas rotas protegidas
curl http://localhost:8080/api/... -H "Authorization: Bearer <accessToken>"
```

O token vale 15 minutos (`stride.jwt.access-token-ttl`).

---

## Estrutura

Pacotes por **funcionalidade**, não por camada técnica. Cada funcionalidade tem o
próprio controller, service e repository:

```
com.stride.api
├── auth/     login, emissão e validação de token
├── user/     cadastro e consulta de usuários
└── config/   segurança, JWT, CORS
```

Regras que valem para todo mundo:

- **SQL só no repository.** Nenhum service ou controller escreve query.
- **Controller é fino.** Recebe, valida, delega, devolve. Sem regra de negócio.
- **DTO de entrada e de saída separados da entidade.** É o que impede o
  `passwordHash` de vazar numa resposta por descuido.
- **Migration nunca é editada depois de aplicada.** O Flyway guarda o checksum e
  falha se o arquivo mudar. Precisou corrigir? Crie uma nova versão.
- Migrations versionadas: `V<versão>__<descrição>.sql` (**dois** underscores).
  Migration repetível (seed): `R__<descrição>.sql`.

---

## Comandos úteis

```bash
./mvnw spring-boot:run          # sobe a aplicação (e o banco)
./mvnw test                     # testes — usam Testcontainers, banco próprio
./mvnw spotless:apply           # formata o código (google-java-format)
./mvnw verify                   # build completo + cobertura em target/site/jacoco
```

O `spotless:check` roda na fase `compile`: **código fora do padrão quebra o
build**. Se isso acontecer, rode `./mvnw spotless:apply` e commite de novo.

Acessando o banco direto:

```bash
docker exec -it stride-backend-postgres-1 psql -U postgres -d stride_db
```
