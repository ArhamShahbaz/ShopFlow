# ShopFlow - Microservices E-Commerce Platform

A microservices-based e-commerce platform built with Spring Boot 4, PostgreSQL, Apache Kafka, and Docker.

## Architecture

| Service              | Port | Database              | Description                        |
|----------------------|------|-----------------------|------------------------------------|
| API Gateway          | 8080 | —                     | Routes requests to microservices   |
| Order Service        | 8081 | shopflow_order        | Manages customer orders            |
| Inventory Service    | 8082 | shopflow_inventory    | Tracks product stock levels        |
| Payment Service      | 8083 | shopflow_payment      | Processes payments                 |
| Product Service      | 8084 | shopflow_product      | Product catalog management         |
| Notification Service | 8085 | shopflow_notification | Sends notifications (email, etc.)  |

### Infrastructure

| Component  | Port | Image               |
|------------|------|----------------------|
| PostgreSQL | 5432 | postgres:16          |
| Kafka      | 9092 | apache/kafka:3.9.0   |

## Prerequisites

- **Java 17** or higher
- **Maven 3.9+**
- **Docker** & **Docker Compose** (v2)

## Quick Start — Start Everything with One Command

### Step 1: Build all services

```bash
mvn clean package -DskipTests
```

This builds all six microservices using the parent POM's `<modules>` configuration.

### Step 2: Start the entire stack

```bash
docker compose up -d --build
```

This single command:
1. Starts **PostgreSQL** and runs the init script (`init-scripts/init-databases.sql`) which creates a separate database and user for each service.
2. Starts **Kafka** in KRaft mode (no Zookeeper needed).
3. Builds and starts all six microservices via the `include` directive, which pulls in each service's own `docker-compose.yml`.

### Step 3: Verify everything is running

```bash
docker compose ps
```

You should see 8 containers running (postgres + kafka + 6 services).

### Step 4: Stop everything

```bash
docker compose down
```

To also remove data volumes:

```bash
docker compose down -v
```

## Project Structure

```
ShopFlow/
├── docker-compose.yml              # Main compose — includes all sub-compose files
├── init-scripts/
│   └── init-databases.sql           # Creates all databases & users
├── pom.xml                          # Parent POM (multi-module)
├── README.md
├── api-gateway/
│   ├── docker-compose.yml           # Service-level compose
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── inventory-service/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── init-scripts/init.sql        # Service-specific DB init
│   ├── pom.xml
│   └── src/
├── notification-service/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── init-scripts/init.sql
│   ├── pom.xml
│   └── src/
├── order-service/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── init-scripts/init.sql
│   ├── pom.xml
│   └── src/
├── payment-service/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── init-scripts/init.sql
│   ├── pom.xml
│   └── src/
└── product-service/
    ├── docker-compose.yml
    ├── Dockerfile
    ├── init-scripts/init.sql
    ├── pom.xml
    └── src/
```

## Database Access

Each service has its own isolated database and user:

| Service      | Database              | Username          | Password          |
|--------------|-----------------------|-------------------|-------------------|
| Inventory    | shopflow_inventory    | inventory_user    | inventory_pass    |
| Order        | shopflow_order        | order_user        | order_pass        |
| Payment      | shopflow_payment      | payment_user      | payment_pass      |
| Product      | shopflow_product      | product_user      | product_pass      |
| Notification | shopflow_notification | notification_user | notification_pass |

Connect to any database:

```bash
docker exec -it shopflow-postgres psql -U inventory_user -d shopflow_inventory
```

## Kafka

Kafka runs in **KRaft mode** (no Zookeeper).

### Test Kafka — Send and Read a Message

**Produce a test message:**

```bash
docker exec -it shopflow-kafka /opt/kafka/bin/kafka-console-producer.sh \
  --broker-list localhost:9092 \
  --topic test-topic
```

Type a message and press Enter, then Ctrl+C to exit.

**Consume the test message:**

```bash
docker exec -it shopflow-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic test-topic \
  --from-beginning
```

## Maven Profiles

### Docker Profile

Start infrastructure as part of the Maven build:

```bash
mvn verify -Pdocker
```

This runs `docker compose up -d --build` during `pre-integration-test` and `docker compose down` during `post-integration-test`.

## Adding Kafka Dependencies

To use Kafka in a service, add this dependency to the service's `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port already in use | `docker compose down` then retry, or change the port mapping |
| Database not created | Check `docker logs shopflow-postgres` for init script errors |
| Kafka not starting | Ensure no other Kafka instance is running on port 9092 |
| Build failure | Run `mvn clean package -DskipTests` before `docker compose up` |
| Container can't reach Postgres | Make sure all services are on `shopflow-network` |
