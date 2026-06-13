# Planeja+ / StudyHub Backend

API REST Spring Boot 3 + PostgreSQL + Flyway.

## Pré-requisitos

- Java 17+
- Docker (PostgreSQL local via docker-compose na raiz do StudyHub)

## Configuração

```bash
cp .env.example .env
export $(grep -v '^#' .env | xargs)
```

Suba o PostgreSQL:

```bash
cd .. && docker compose up -d
```

## Executar

```bash
./mvnw spring-boot:run
```

## Testes

```bash
./mvnw verify
```

Usa Testcontainers (PostgreSQL) — Docker necessário para testes de integração.

## Endpoints principais

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/auth/register` | Cadastro |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/refresh` | Renovar access token (cookie) |
| POST | `/api/auth/logout` | Logout |
| POST | `/api/auth/password-reset/request` | Solicitar reset |
| POST | `/api/auth/password-reset/confirm` | Confirmar reset |
| GET | `/actuator/health` | Health check |

## Segurança

- BCrypt + JWT access token (15 min)
- Refresh token em cookie httpOnly (7 dias)
- Rate limiting: 10 req/min em `/api/auth/**`
- Multi-tenancy por `usuario_id`
- Schema versionado via Flyway
