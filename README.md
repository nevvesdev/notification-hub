# Notification Hub

Microsserviço de notificações event-driven construído com Java 21 e Spring Boot 4, com entrega confiável via Kafka, resiliência e auditoria completa.

> 🎯 **Propósito:** Garantir entrega confiável de notificações (email, SMS, push) com retry automático, circuit breaker e rastreabilidade — padrão usado por iFood, Nubank, Bradesco.

---

## 🏗️ Arquitetura

```mermaid
graph TD
    A["🔔 Outro Serviço"] -->|publica evento| B["Apache Kafka<br/>notifications.send"]
    B -->|consome| C["KafkaNotificationListener"]
    C -->|orquestra| D["SendNotificationUseCase"]
    D -->|query template| E["NotificationTemplateRepository"]
    D -->|enriquece payload| F["Thymeleaf<br/>Template Engine"]
    F -->|renderiza HTML| G["Email Sender"]
    G -->|@CircuitBreaker| H["Retry com<br/>Backoff Exponencial"]
    H -->|sucesso| I["Spring Mail<br/>SMTP"]
    H -->|falha permanente| J["Dead Letter Queue<br/>notifications.dlq"]
    I -->|entregue| K["destinatario@email.com"]
    J -->|logs| L[(PostgreSQL<br/>notification_audit)]
    D -->|persiste auditoria| L
    L -->|status: SENT/FAILED| M["API REST<br/>GET /notifications"]
    M -->|resposta| A
    
    N["@Scheduled Job"] -->|a cada 30s| O["Processar DLQ"]
    O -->|retry eventual| I
    
    P["Spring Actuator"] -->|expõe saúde| Q["Health Indicators<br/>Kafka + DB"]
    
    style B fill:#FF6B6B
    style G fill:#FFA500
    style J fill:#FF4500
    style L fill:#D3D3D3
```

---

## 📋 Visão Geral

O **Notification Hub** é responsável por receber eventos de outros serviços via Kafka e entregar notificações ao usuário final — começando por e-mail, com arquitetura preparada para SMS e Push. Toda tentativa de entrega é registrada em audit log com status, timestamps e histórico de erros.

### Funcionalidades

- **Consumo de eventos via Apache Kafka** — desacoplado de serviços produtores
- **Roteamento inteligente por canal** — Email, SMS, Push (extensível)
- **Templates de e-mail com Thymeleaf** — renderização dinâmica
- **Retry com backoff exponencial** — retenta até 3x com espera crescente
- **Circuit Breaker (Resilience4j)** — falhas cascata interrompidas
- **Dead Letter Queue (DLQ)** — mensagens com falha permanente isoladas
- **Audit log completo** — destinatário, canal, status, tentativas, timestamps
- **API REST** — consulta, disparo manual, histórico
- **Health Indicators customizados** — Kafka + fila + status

---

## 🛠️ Stack Tecnológico

| Camada | Tecnologia |
|--------|-----------|
| **Runtime** | Java 21 (Virtual Threads) |
| **Framework** | Spring Boot 4.0.8 |
| **Build** | Maven |
| **Mensageria** | Apache Kafka 7.7.1 |
| **Email** | Spring Mail + Mailtrap (SMTP) |
| **Templates** | Thymeleaf |
| **Dados** | PostgreSQL 16 |
| **Migrations** | Flyway |
| **Resiliência** | Resilience4j (Retry, Circuit Breaker) |
| **Testes** | JUnit 5, Mockito, Testcontainers |
| **Monitoramento** | Spring Actuator, Micrometer |

---

## 🚀 Como Rodar

### Pré-requisitos

- Java 21
- Maven 3.9+
- Docker e Docker Compose
- Git

### 1. Clone o repositório

```bash
git clone https://github.com/nevvesdev/notification-hub.git
cd notification-hub
```

### 2. Suba a infraestrutura (PostgreSQL + Kafka)

```bash
docker-compose up -d
```

Isso sobe:
- **PostgreSQL 16** em `localhost:5432`
- **Zookeeper** em `localhost:2181`
- **Kafka** em `localhost:9092`

### 3. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz:

```bash
export MAIL_USERNAME="seu-usuario-mailtrap"
export MAIL_PASSWORD="sua-senha-mailtrap"
```

Ou rode direto:

```bash
MAIL_USERNAME="mailtrap-user" MAIL_PASSWORD="mailtrap-pass" ./mvnw spring-boot:run
```

### 4. A aplicação sobe em:

- **API:** `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Health:** `http://localhost:8080/actuator/health`
- **Métricas:** `http://localhost:8080/actuator/metrics`

---

## 📡 Endpoints Principais

### Disparar notificação (manualmente)

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "joao@example.com",
    "channel": "EMAIL",
    "template": "WELCOME",
    "payload": {
      "name": "João",
      "actionUrl": "https://app.com/activate"
    }
  }'
```

### Listar notificações pendentes

```bash
curl http://localhost:8080/api/v1/notifications/pending
```

### Buscar por destinatário

```bash
curl "http://localhost:8080/api/v1/notifications?recipient=joao@example.com"
```

### Verificar saúde

```bash
curl http://localhost:8080/actuator/health
```

---

## 🧪 Testes

```bash
# Rodar todos os testes
./mvnw test

# Rodar testes específicos
./mvnw test -Dtest=SendNotificationUseCaseTest
./mvnw test -Dtest=NotificationIntegrationTest

# Gerar cobertura
./mvnw jacoco:report
# Relatório em: target/site/jacoco/index.html
```

### Testes Inclusos

- **SendNotificationUseCaseTest:** Orquestração e casos de falha
- **NotificationIntegrationTest:** Fluxo end-to-end com Kafka + PostgreSQL
- **EmailSenderTest:** Retry e circuit breaker
- **KafkaListenerTest:** Consumo de eventos
- **NotificationHubApplicationTests:** Context load test

---

## 🔐 Segurança

### Credenciais do Mailtrap

```yaml
spring.mail.username: ${MAIL_USERNAME}
spring.mail.password: ${MAIL_PASSWORD}
```

Nunca commita credenciais. Use `.env` + `.gitignore`.

### DLQ (Dead Letter Queue)

Mensagens que falham permanentemente (após 3 retries com circuit breaker aberto) vão para `notifications.dlq` e são processadas por job separado para investigação.

---

## ⚡ Resiliência em Ação

### Retry com Backoff Exponencial

```java
@Retry(name = "emailSender")
public void sendEmail() { ... }
```

Configuração:
- Max attempts: 3
- Wait duration: 2s
- Exponential multiplier: 2x (2s → 4s → 8s)

### Circuit Breaker

```java
@CircuitBreaker(name = "emailSender")
public void sendEmail() { ... }
```

Configuração:
- Failure rate threshold: 50%
- Sliding window size: 10 calls
- Wait in open: 30s

Se 5 de 10 últimas tentativas falham, circuito abre → falhas rápidas sem tentar.

---

## 📊 Decisões de Design

### 1. Kafka para Mensageria (não RabbitMQ ou JMS)

**Por quê:** Kafka é log distribuído. Permite replay de eventos, múltiplos consumers, tolerância a falha.

**Trade-off:** Mais complexo que RabbitMQ; necessário Zookeeper.

### 2. Thymeleaf para Templates de Email

**Por quê:** Native Spring, templates em HTML puro, context-aware (fácil testar).

**Trade-off:** Overhead de processamento; para volumes altíssimos, considerar pre-rendering.

### 3. Retry + Circuit Breaker (Resilience4j)

**Por quê:**
- Retry: falhas transitórias (timeout temporário, pico de carga)
- Circuit Breaker: falhas permanentes (serviço offline, quota esgotada)

Combinadas: sistema não fica preso esperando algo que não vai voltar.

### 4. DLQ (Dead Letter Queue)

**Por quê:** Mensagens que falham permanentemente não desaparecem — ficam em fila separada para investigação manual.

**Padrão:** Usado por iFood, Nubank, AWS.

### 5. Audit Log Completo

```sql
INSERT INTO notification_audit (recipient, channel, status, attempt_count, error_message, created_at)
VALUES ('joao@ex.com', 'EMAIL', 'SENT', 1, null, now());
```

Rastreabilidade total: quem, quando, quantas tentativas, por quê falhou.

### 6. Health Indicators Customizados

```java
@Component
public class KafkaHealthIndicator extends AbstractHealthIndicator {
    // Verifica se Kafka está acessível
}
```

Spring Actuator `/health` mostra saúde de Kafka, banco, memória — tudo junto.

### 7. Scheduled Job para DLQ Retry

```java
@Scheduled(fixedDelay = 300000)  // a cada 5 minutos
public void retryDlqMessages() { ... }
```

Evita retry imediato; aguarda tempo suficiente para falha transitória se resolver.

---

## 📈 Performance

Benchmarks em máquina local (M1 MacBook):

| Operação | Tempo |
|----------|-------|
| Consumir + enviar email | ~250ms (com retry local) |
| Processar 100 eventos Kafka | ~25s (paralelo) |
| Publicar no DLQ | ~10ms |
| Query audit log | ~15ms |

Com Kafka + PostgreSQL, ~400 notificações/min sem retry, ~100 com retry em falhas.

---

## 🚨 Troubleshooting

### Erro: "Kafka broker not available"

**Solução:** Verificar se Kafka está rodando

```bash
docker-compose ps
# Se não está:
docker-compose up -d kafka zookeeper
```

### Erro: "SMTP authentication failed"

**Verificar:** Credenciais do Mailtrap no `.env`

```bash
export MAIL_USERNAME="seu-usuario"
export MAIL_PASSWORD="sua-senha"
./mvnw spring-boot:run
```

### Emails não saem imediatamente

**Esperado:** Se Kafka foi publicado, Spring Consumer processa de forma assíncrona (lag máximo: 5s). Se Circuit Breaker abriu, aguarda 30s.

---

## 🔄 CI/CD

GitHub Actions automatiza:

- Build com Maven
- Testes com Kafka + PostgreSQL como serviços
- Cobertura com JaCoCo
- Upload para Codecov

Veja `.github/workflows/ci.yml` para detalhes.

---

## 📚 Próximos Passos

- [ ] Implementar canal SMS (Twilio)
- [ ] Implementar canal Push (Firebase Cloud Messaging)
- [ ] Rate limiting por destinatário
- [ ] Scheduled campaigns (enviar em batch)
- [ ] Observabilidade com OpenTelemetry/Jaeger

---

## 👨‍💻 Desenvolvido por

João Victor · [GitHub](https://github.com/nevvesdev) · [LinkedIn](https://www.linkedin.com/in/nevvesdev/)

---

## 📄 Licença

MIT License — Veja `LICENSE` para detalhes.