# Planeja+ Backend

API REST desenvolvida com Spring Boot 3 + Java para o app de planejamento de estudos **Planeja+**.

---

## Pré-requisitos

- Java 17+
- Maven (ou use o wrapper `./mvnw` incluído no projeto)
- MySQL rodando localmente na porta `3306`

---

## Instalação e configuração

### 1. Clone o projeto

```bash
git clone <url-do-repo>
cd sistema-estudos-backend
```

### 2. Configure as variáveis de ambiente

Copie o arquivo de exemplo e preencha com seus valores:

```bash
cp .env.example .env
```

Edite o `.env`:

```env
DB_URL=jdbc:mysql://localhost:3306/planeja_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
DB_USERNAME=root
DB_PASSWORD=sua_senha
JWT_SECRET=coloque-uma-chave-secreta-forte-aqui
```

> **Atenção:** nunca suba o arquivo `.env` para o repositório. Ele já está no `.gitignore`.

### 3. Crie o banco de dados (opcional)

O banco é criado automaticamente pela aplicação (`createDatabaseIfNotExist=true`). Se preferir criar manualmente:

```sql
CREATE DATABASE IF NOT EXISTS planeja_db;
```

### 4. Dê permissão ao wrapper Maven (Linux/macOS)

```bash
chmod +x mvnw
```

### 5. Suba a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em: **http://localhost:8080**

---

## Variáveis de ambiente

| Variável     | Descrição                        | Padrão (desenvolvimento)         |
|--------------|----------------------------------|----------------------------------|
| `DB_URL`     | JDBC URL completa do MySQL       | `jdbc:mysql://localhost:3306/planeja_db` |
| `DB_USERNAME`| Usuário do banco                 | `root`                           |
| `DB_PASSWORD`| Senha do banco                   | *(vazio)*                        |
| `JWT_SECRET` | Chave secreta para assinar JWTs  | `minha-chave-secreta-desenvolvimento` |

Em produção, defina essas variáveis no ambiente do servidor ou no serviço de deploy (Railway, Render, Heroku, etc.) — nunca hardcode credenciais no código.

---

## Endpoints

### Autenticação (público)

| Método | Rota                  | Descrição              |
|--------|-----------------------|------------------------|
| POST   | `/api/auth/register`  | Cadastrar novo usuário |
| POST   | `/api/auth/login`     | Login — retorna JWT    |

**Exemplo de registro:**
```json
POST /api/auth/register
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "senha": "minhasenha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "nome": "João Silva",
  "email": "joao@email.com"
}
```

---

### Rotas protegidas (requer header `Authorization: Bearer <token>`)

#### Disciplinas

| Método | Rota                     | Descrição          |
|--------|--------------------------|--------------------|
| GET    | `/api/disciplinas`       | Listar disciplinas |
| POST   | `/api/disciplinas`       | Criar disciplina   |
| PUT    | `/api/disciplinas/{id}`  | Editar disciplina  |
| DELETE | `/api/disciplinas/{id}`  | Excluir disciplina |

**Corpo para criar/editar:**
```json
{ "nome": "Matemática" }
```

#### Assuntos

| Método | Rota                             | Descrição                      |
|--------|----------------------------------|--------------------------------|
| GET    | `/api/assuntos/{disciplinaId}`   | Listar assuntos da disciplina  |
| POST   | `/api/assuntos`                  | Criar assunto                  |
| PATCH  | `/api/assuntos/{id}/concluir`    | Marcar assunto como concluído  |

**Corpo para criar assunto:**
```json
{
  "titulo": "Equações do 2º grau",
  "dataProgramada": "2024-06-10",
  "disciplinaId": 1
}
```

#### Painel Diário

| Método | Rota                 | Descrição                                                           |
|--------|----------------------|---------------------------------------------------------------------|
| GET    | `/api/painel-diario` | Retorna assuntos do dia e move pendentes anteriores automaticamente |

---

## Segurança

- Senhas armazenadas com **BCrypt**
- Autenticação via **JWT** com validade de **24 horas**
- Cada usuário acessa apenas seus próprios dados
- CORS liberado para `http://localhost:3000`

---

## Estrutura do projeto

```
src/main/java/com/planejamais/
├── config/          # SecurityConfig, CorsConfig, GlobalExceptionHandler
├── controller/      # AuthController, DisciplinaController, AssuntoController, PainelDiarioController
├── dto/             # DTOs de request e response
├── entity/          # Usuario, Disciplina, Assunto
├── repository/      # Interfaces JPA
├── security/        # JwtUtil, JwtFilter, UserDetailsServiceImpl
└── service/         # AuthService, DisciplinaService, AssuntoService, PainelDiarioService
```
