# Notification Hub

Microsserviço de notificações event-driven construído com Java 21 e Spring Boot 4.

## Visão Geral

O **Notification Hub** é responsável por receber eventos de outros serviços via Kafka e entregar notificações ao usuário final — começando por e-mail, com arquitetura preparada para SMS e Push. Toda tentativa de entrega é registrada em audit log com status, timestamps e histórico de erros.

## Propósito de negócio

Centralizar e garantir a entrega confiável de notificações, com rastreabilidade completa e resiliência a falhas transitórias.

## Funcionalidades

- Consumo de eventos via Apache Kafka
- Roteamento inteligente por canal (Email, SMS, Push)
- Templates de e-mail com Thymeleaf
- Retry com backoff exponencial e Circuit Breaker (Resilience4j)
- Dead Letter Queue para mensagens com falha permanente
- Audit log completo (destinatário, canal, status, tentativas, timestamps)
- API REST para consulta e disparo de notificações
- Health indicators customizados (Kafka + fila de notificações)

## Stack

| Tecnologia | Uso |
|---|---|
| Java 21 | Virtual Threads, Records |
| Spring Boot 4.0.8 | Framework principal |
| Apache Kafka | Mensageria assíncrona |
| Spring Mail + Mailtrap | Envio de e-mails |
| Thymeleaf | Templates de e-mail |
| PostgreSQL | Persistência do audit log |
| Flyway | Migrações de banco |
| Resilience4j | Retry, Circuit Breaker |
| Testcontainers | Testes de integração |

## Arquitetura

```
src/main/java/br/com/nevvesdev/notificationhub/
├── domain/           # Entidades, VOs, regras de negócio puras
├── application/      # Casos de uso, ports (interfaces)
├── infrastructure/   # Adapters: Kafka, JPA, Mail, Health
├── api/              # Controllers REST
└── shared/           # Exceções e utilitários compartilhados
```

## Como rodar localmente

### Pré-requisitos

- Java 21
- Docker e Docker Compose

### Subindo a infra

```bash
make up
```

### Rodando a aplicação

```bash
make run
```

### Rodando os testes

```bash
make test
```

## Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `MAIL_USERNAME` | Usuário do Mailtrap |
| `MAIL_PASSWORD` | Senha do Mailtrap |

## Testando a API

### Base URL

```
http://localhost:8080
```

### 1. Disparar uma notificação de boas-vindas

```bash
curl -s -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "joao@nevvesdev.com.br",
    "channel": "EMAIL",
    "template": "WELCOME",
    "payload": {
      "name": "João",
      "actionUrl": "https://app.nevvesdev.com.br/login"
    }
  }' | jq
```

### 2. Disparar uma notificação de redefinição de senha

```bash
curl -s -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "joao@nevvesdev.com.br",
    "channel": "EMAIL",
    "template": "PASSWORD_RESET",
    "payload": {
      "resetUrl": "https://app.nevvesdev.com.br/reset?token=abc123",
      "expiresIn": "30 minutos"
    }
  }' | jq
```

### 3. Disparar uma notificação de pedido confirmado

```bash
curl -s -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "joao@nevvesdev.com.br",
    "channel": "EMAIL",
    "template": "ORDER_CONFIRMED",
    "payload": {
      "name": "João",
      "orderId": "PED-00123",
      "total": "R$ 349,90",
      "deliveryDate": "20/09/2026",
      "trackingUrl": "https://app.nevvesdev.com.br/orders/PED-00123"
    }
  }' | jq
```

### 4. Buscar notificação por ID

```bash
curl -s http://localhost:8080/api/v1/notifications/{id} | jq
```

### 5. Listar notificações por destinatário

```bash
curl -s "http://localhost:8080/api/v1/notifications?recipient=joao@nevvesdev.com.br" | jq
```

### 6. Listar notificações pendentes

```bash
curl -s http://localhost:8080/api/v1/notifications/pending | jq
```

### 7. Verificar saúde da aplicação

```bash
curl -s http://localhost:8080/actuator/health | jq
```

### 8. Verificar métricas

```bash
curl -s http://localhost:8080/actuator/metrics | jq
```

### 9. Publicar evento direto no Kafka (simula outro serviço)

```bash
docker exec -it notification-hub-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic notifications.send \
  --property "value.serializer=org.apache.kafka.common.serialization.StringSerializer"
```

Cole o payload e pressione Enter:

```json
{"recipient":"joao@nevvesdev.com.br","channel":"EMAIL","template":"WELCOME","payload":{"name":"João"}}
```

### Exemplos de resposta

**201 Created — sucesso**
```json
{
  "success": true,
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "recipient": "joao@nevvesdev.com.br",
    "channel": "EMAIL",
    "template": "WELCOME",
    "status": "SENT",
    "attemptCount": 1,
    "createdAt": "2026-09-14T01:00:00Z",
    "updatedAt": "2026-09-14T01:00:01Z"
  },
  "message": "Notification queued successfully",
  "timestamp": "2026-09-14T01:00:01Z"
}
```

**422 Unprocessable Entity — e-mail inválido**
```json
{
  "status": 422,
  "error": "INVALID_RECIPIENT",
  "message": "Invalid email address: not-an-email",
  "timestamp": "2026-09-14T01:00:01Z"
}
```

**400 Bad Request — body inválido**
```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Invalid request body",
  "fields": [
    { "field": "channel", "message": "Channel is required" }
  ],
  "timestamp": "2026-09-14T01:00:01Z"
}
```

## Fases de desenvolvimento

- [x] Fase 0 — Fundação (estrutura, infra, baseline SQL)
- [x] Fase 1 — Domínio (entidades, VOs, enums)
- [x] Fase 2 — Casos de uso e ports
- [x] Fase 3 — Kafka Consumer + JPA Adapter
- [x] Fase 4 — Email Adapter (Thymeleaf)
- [x] Fase 5 — Resiliência (Retry, Circuit Breaker, DLQ)
- [x] Fase 6 — API REST
- [x] Fase 7 — Testes (Testcontainers)
- [x] Fase 8 — Observabilidade