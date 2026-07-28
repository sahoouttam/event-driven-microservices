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

## 🎯 Key Features

- **Event-Driven Architecture**: Services communicate via Apache Kafka events
- **Outbox Pattern**: Guarantees exactly-once event publishing
- **SAGA Orchestration**: Choreography-based distributed transaction management
- **Kafka Streams**: Real-time order validation and enrichment
- **Database per Service**: Each service owns its data (PostgreSQL)
- **Async Processing**: Non-blocking parallel calls with CompletableFuture

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

## 📊 Event Flow

| Step | Publisher | Event | Consumer | Action |
|------|-----------|-------|----------|--------|
| 1 | Order Service | `ORDER_CREATED` | Stock Service | Reserve inventory (all-or-nothing) |
| 2 | Stock Service | `STOCK_RESERVATION_UPDATED` | Order Service | Update to PENDING_PAYMENT |
| 3 | Stock Service | `STOCK_RESERVATION_FAILED` | Order Service | Update to FAILED |
| 4 | Order Service | `ORDER_PAYMENT_INITIATED` | Payment Service | Process payment |
| 5 | Payment Service | `PAYMENT_COMPLETED` | Order Service | Update to CONFIRMED |
| 6 | Payment Service | `PAYMENT_FAILED` | Order Service | Update to CANCELLED |
| 7 | Order Service | `ORDER_CONFIRMED` | Stock, Fulfillment | Deduct stock, create shipment |
| 8 | Order Service | `ORDER_CANCELLED` | Stock, Fulfillment | Release stock, cancel fulfillment |
| 9 | Fulfillment Service | `FULFILLMENT_SHIPPED` | Order Service | Update to SHIPPED |
| 10 | Fulfillment Service | `FULFILLMENT_DELIVERED` | Order Service | Update to DELIVERED |
| 11 | Fulfillment Service | `RETURN_COMPLETED` | Order, Stock | Update status, restock |

## 🗄️ Kafka Topics

| Topic | Events | Publisher | Consumer |
|-------|--------|-----------|----------|
| `order-events` | ORDER_CREATED, ORDER_CONFIRMED, ORDER_CANCELLED | Order Service | Stock, Fulfillment |
| `inventory-events` | STOCK_RESERVATION_UPDATED, STOCK_RESERVATION_FAILED, STOCK_UPDATED | Stock Service | Order Service |
| `payment-events` | ORDER_PAYMENT_INITIATED, PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_REFUNDED | Order, Payment | Payment, Order |
| `fulfillment-events` | FULFILLMENT_SHIPPED, FULFILLMENT_DELIVERED, RETURN_COMPLETED | Fulfillment | Order, Stock |

