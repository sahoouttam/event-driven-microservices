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

```mermaid
sequenceDiagram
    participant C as Customer
    participant O as Order Service
    participant K as Kafka
    participant S as Stock Service
    participant P as Payment Service
    participant F as Fulfillment Service

    C->>O: POST /orders
    O->>O: Create Order (PENDING_INVENTORY)
    O->>K: ORDER_CREATED
    K->>S: Consume ORDER_CREATED
    S->>S: Reserve Stock
    S->>K: STOCK_RESERVED
    K->>O: Consume STOCK_RESERVED
    O->>O: Update Status (PENDING_PAYMENT)
    O->>K: ORDER_PAYMENT_INITIATED
    K->>P: Consume ORDER_PAYMENT_INITIATED
    P->>P: Process Payment
    P->>K: PAYMENT_COMPLETED
    K->>O: Consume PAYMENT_COMPLETED
    O->>O: Update Status (CONFIRMED)
    O->>K: ORDER_CONFIRMED
    K->>S: Consume ORDER_CONFIRMED
    S->>S: Deduct Stock
    K->>F: Consume ORDER_CONFIRMED
    F->>F: Create Fulfillment
    F->>K: FULFILLMENT_SHIPPED
    K->>O: Consume FULFILLMENT_SHIPPED
    O->>O: Update Status (SHIPPED)

sequenceDiagram
    participant O as Order Service
    participant K as Kafka
    participant S as Stock Service
    participant P as Payment Service

    Note over O,P: Scenario 1: Stock Reservation Failed
    O->>K: ORDER_CREATED
    K->>S: Consume ORDER_CREATED
    S->>S: Insufficient Stock!
    S->>K: STOCK_RESERVATION_FAILED
    K->>O: Consume STOCK_RESERVATION_FAILED
    O->>O: Update Status (FAILED)
    O->>K: ORDER_CANCELLED
    K->>S: Consume ORDER_CANCELLED
    S->>S: Release Reservation

    Note over O,P: Scenario 2: Payment Failed
    O->>K: ORDER_PAYMENT_INITIATED
    K->>P: Consume ORDER_PAYMENT_INITIATED
    P->>P: Payment Declined!
    P->>K: PAYMENT_FAILED
    K->>O: Consume PAYMENT_FAILED
    O->>O: Update Status (CANCELLED)
    O->>K: ORDER_CANCELLED
    K->>S: Consume ORDER_CANCELLED
    S->>S: Release Reservation

sequenceDiagram
    participant C as Customer
    participant F as Fulfillment Service
    participant K as Kafka
    participant O as Order Service
    participant S as Stock Service
    participant P as Payment Service

    C->>F: POST /returns (Initiate Return)
    F->>F: Create Return (INITIATED)
    F->>K: RETURN_COMPLETED
    K->>O: Consume RETURN_COMPLETED
    O->>O: Update Status (RETURNED)
    K->>S: Consume RETURN_COMPLETED
    S->>S: Restock Items
    O->>P: Process Refund
    P->>K: PAYMENT_REFUNDED
    K->>O: Consume PAYMENT_REFUNDED
    O->>O: Update Status (PAYMENT_REFUNDED)

stateDiagram-v2
    [*] --> PENDING_INVENTORY
    PENDING_INVENTORY --> PENDING_PAYMENT: Stock Reserved
    PENDING_INVENTORY --> FAILED: Stock Failed
    PENDING_PAYMENT --> CONFIRMED: Payment Success
    PENDING_PAYMENT --> CANCELLED: Payment Failed
    CONFIRMED --> SHIPPED: Order Shipped
    SHIPPED --> DELIVERED: Order Delivered
    DELIVERED --> RETURNED: Return Completed
    RETURNED --> PAYMENT_REFUNDED: Refund Processed
    CANCELLED --> [*]
    FAILED --> [*]
    PAYMENT_REFUNDED --> [*]

flowchart TD
    A[Order Created] --> B{Stock Check}
    B -->|Available| C[Reserve Stock]
    B -->|Unavailable| D[Order Failed]
    D --> E[Compensate: Nothing to undo]
    
    C --> F{Payment}
    F -->|Success| G[Order Confirmed]
    F -->|Failed| H[Order Cancelled]
    H --> I[Compensate: Release Stock]
    
    G --> J[Deduct Stock]
    G --> K[Create Fulfillment]
    
    G --> L{Refund Requested?}
    L -->|Yes| M[Refund Payment]
    M --> N[Compensate: Restock Items]

graph TD
    subgraph "Event Bus (Apache Kafka)"
        OE[order-events]
        IE[inventory-events]
        PE[payment-events]
        FE[fulfillment-events]
    end

    subgraph "Services"
        OS[Order Service<br>:8082]
        SS[Stock Service<br>:8081]
        PS[Payment Service<br>:8083]
        AS[Account Service<br>:8088]
        FS[Fulfillment Service<br>:8085]
        ES[Enrichment Streams<br>:8084]
    end

    OS -->|ORDER_CREATED<br>ORDER_CONFIRMED<br>ORDER_CANCELLED| OE
    SS -->|STOCK_RESERVED<br>STOCK_RESERVATION_FAILED| IE
    OS -->|ORDER_PAYMENT_INITIATED| PE
    PS -->|PAYMENT_COMPLETED<br>PAYMENT_FAILED<br>PAYMENT_REFUNDED| PE
    FS -->|FULFILLMENT_SHIPPED<br>FULFILLMENT_DELIVERED<br>RETURN_COMPLETED| FE

    OE --> SS
    OE --> FS
    IE --> OS
    PE --> PS
    PE --> OS
    FE --> OS
    FE --> SS



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

