# StudyHub

Hub de estudos SaaS para vestibulandos e concurseiros — Spring Boot + React + PostgreSQL.

## Estrutura

```
StudyHub/
├── docker-compose.yml              # PostgreSQL local
├── sistema-estudos-backend/        # API REST (Spring Boot 3)
└── sistema-estudos-frontend/
    └── planeja-app/                # SPA React
```

## Pré-requisitos

- Java 17+
- Node.js 18+
- Docker (para PostgreSQL local)

## Subir stack local

### 1. Banco de dados

```bash
cd StudyHub
docker compose up -d
```

O PostgreSQL do Docker fica na porta **5433** (evita conflito se você já tiver Postgres na 5432).

**DBeaver:** host `localhost`, porta `5433`, database `studyhub`, user/senha `studyhub`.

Se já tiver PostgreSQL local na 5432, pode usá-lo direto — ajuste `DB_URL` no `.env` do backend.

### 2. Backend

```bash
cd sistema-estudos-backend
cp .env.example .env
# Exporte as variáveis ou configure no IDE
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run
```

API: http://localhost:8080  
Health: http://localhost:8080/actuator/health  
Swagger (dev): http://localhost:8080/swagger-ui.html

### 3. Frontend

```bash
cd sistema-estudos-frontend/planeja-app
cp .env.example .env
npm install
npm start
```

App: http://localhost:3000

## Variáveis de ambiente (backend)

| Variável | Descrição | Padrão |
|---|---|---|
| `DB_URL` | JDBC PostgreSQL | `jdbc:postgresql://localhost:5433/studyhub` |
| `DB_USERNAME` | Usuário do banco | `studyhub` |
| `DB_PASSWORD` | Senha do banco | `studyhub` |
| `JWT_SECRET` | Chave JWT (mín. 32 chars) | — |
| `JWT_ACCESS_EXPIRATION` | Expiração access token (ms) | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION` | Expiração refresh token (ms) | `604800000` (7 dias) |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas | `http://localhost:3000` |
| `SPRING_PROFILES_ACTIVE` | Profile Spring | `dev` |

## Fundação implementada

- PostgreSQL + Flyway (migrations versionadas)
- JWT access token em memória (frontend) + refresh httpOnly cookie
- Rate limiting auth (10/min) + API (120/min) por IP
- Bloqueio de conta após 5 logins falhos (15 min)
- Header anti-CSRF (`X-StudyHub-Client`) em refresh/logout
- Senha forte (8+ chars, letra e número)
- Security headers (HSTS em prod, X-Frame-Options, nosniff)
- Multi-tenancy por `usuario_id`
- Exclusão de conta com cascade
- Testes de integração (Testcontainers)
- CI/CD GitHub Actions + Dependabot
- Spring Actuator (health check)

## Branch de evolução

Desenvolvimento da fundação SaaS: branch `evolucoes` (backend e frontend).
