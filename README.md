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
- API REST para consulta de notificações

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
├── infrastructure/   # Adapters: Kafka, JPA, Mail
├── api/              # Controllers REST
└── shared/           # Exceções e utilitários compartilhados
```