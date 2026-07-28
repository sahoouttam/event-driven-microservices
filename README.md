# Event-Driven Microservices Architecture

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.7-red)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

**Production-grade event-driven microservices with Kafka, Outbox Pattern, and SAGA orchestration**

</div>

---

## 🏗️ Architecture Overview
┌─────────────────────────────────────────────────────────────────────────┐
│ API Gateway (Future) │
└─────────────────────────────────────────────────────────────────────────┘
│
┌───────────────┬───────────────┼───────────────┬───────────────┐
↓ ↓ ↓ ↓ ↓
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────────┐
│ Order │ │ Stock │ │Payment │ │Account │ │Fulfillment │
│Service │ │Service │ │Service │ │Service │ │ Service │
└───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └─────┬──────┘
│ │ │ │ │
└────────────┴────────────┴────────────┴──────────────┘
│
┌───────┴────────┐
│ Apache Kafka │
│ (Event Bus) │
└───────┬────────┘
│
┌───────┴────────┐
│ Enrichment │
│ Streams │
│ (Kafka Streams)│
└────────────────┘

## 🎯 Key Features

- **Event-Driven Architecture**: Services communicate via Apache Kafka events
- **Outbox Pattern**: Guarantees exactly-once event publishing
- **SAGA Orchestration**: Choreography-based distributed transaction management
- **Kafka Streams**: Real-time order validation and enrichment
- **Database per Service**: Each service owns its data (PostgreSQL)
- **Async Processing**: Non-blocking parallel calls with CompletableFuture
- **All-or-Nothing Stock Validation**: Ensures order integrity
- **Event Sourcing**: Complete audit trail of all state changes

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5 |
| **Messaging** | Apache Kafka 3.7 |
| **Stream Processing** | Kafka Streams, Avro |
| **Database** | PostgreSQL 16 |
| **Migrations** | Flyway |
| **Build** | Maven (Multi-module) |
| **Testing** | JUnit 5, Mockito |
| **API Docs** | SpringDoc OpenAPI (Swagger) |

## 📦 Services

| Service | Port | Responsibility |
|---------|------|----------------|
| **common-service** | - | Shared DTOs, Events (`EventEnvelope`, `OrderCreatedEvent`, etc.), Enums, Exceptions |
| **account-service** | 8088 | Customer management, Addresses, Loyalty accounts |
| **order-service** | 8082 | Order creation, Orchestration, Status management |
| **stock-service** | 8081 | Product catalog, Inventory, Stock reservation |
| **payment-service** | 8083 | Payment processing, Refunds, Transaction audit |
| **fulfillment-service** | 8085 | Shipping, Tracking, Returns management |
| **order-enrichment-streams** | 8084 | Real-time order validation via Kafka Streams |

## 🔄 Complete Order Flow
1. Customer places order
POST /api/v1/orders

2. Stock validation
├── All items available → Reserve stock → PENDING_PAYMENT
└── Any item unavailable → FAILED (no partial reservation)

3. Payment processing
├── Success → CONFIRMED → Deduct stock → Fulfillment created
└── Failure → CANCELLED → Release reservation

4. Fulfillment
PENDING → PICKING → PACKED → SHIPPED → DELIVERED

5. Return
INITIATED → APPROVED → RECEIVED → COMPLETED → Restock
