# Transactions Processor

Simple REST API application for importing bank account transactions on a **monthly basis** and presenting **aggregated
statistics** to better understand and manage a personal budget.

---

## 🧱 Architecture Overview

The application is a Spring Boot 4 (Java 21) service built around a few clear bounded contexts:

- **importing** – handles CSV import lifecycle (processing / completed / failed)
- **transaction** – parsing, validation and persistence of transactions
- **statistics** – aggregation and querying of imported data
- **security** – JWT-based authentication with workspace isolation

### Key design decisions

- **MongoDB** as primary datastore (preferred by task requirements)
- **YearMonth** used as a first-class concept for monthly imports
- **UUID** used as document identifiers
- **MapStruct** for mapping between layers
- **Testcontainers** for realistic integration tests
- **JWT workspace isolation** – `workspaceId` is taken from token claims, not URL parameters

---

## 📦 Tech Stack

- Java 21
- Spring Boot 4.0.x
- Spring Web / Validation / Security
- Spring Data MongoDB
- MongoDB 7 (replica set enabled)
- MapStruct
- Lombok
- Testcontainers
- Gradle
- Docker / Docker Compose

---

## 🔐 Security Model

The application uses **JWT-based authentication** backed by Keycloak **(OpenID Connect)**.

- Each request must contain an `Authorization: Bearer <jwt>` header
- JWT must contain a `workspaceId` claim
- `workspaceId` is resolved once and stored in the `SecurityContext`
- Controllers and services do **not** accept workspaceId as a request parameter

### Generating JWTs for testing

JWT tokens are issued by Keycloak, which is started as part of the provided docker-compose setup.

Keycloak configuration:

URL: http://localhost:8081

Realm: transactions:

- Client ID: transactions-processor
- Client type: confidential
- Authentication: OAuth2 / OpenID Connect
- Custom claim: workspaceId (configured via protocol mapper)

Example token request

```curl
curl -X POST "http://localhost:8081/realms/transactions/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=transactions-processor" \
  -d "client_secret=transactions-processor-secret" \
  -d "grant_type=password" \
  -d "username=test-user" \
  -d "password=test-password"
```

Example JWT payload:

```json
{
  "sub": "test-user",
  "workspaceId": "workspace-1",
  "iat": 1700000000,
  "exp": 1900000000
}
```

Any standard JWT tool (e.g. jwt.io) can be used to generate a token using the configured secret.

---

## 🐳 Running the Application

### Requirements

- Java 21+
- Docker + Docker Compose

### Start infrastructure

```bash
docker-compose up -d
```

This will start:

- MongoDB 7 (configured as a replica set – required for transactions)

### Start the application

```bash
./gradlew bootRun
```

The application will start on:

```
http://localhost:8080
```

---

## 📥 Importing Transactions

### Import CSV for a given month

```
POST /api/import/{yearMonth}
```

- `yearMonth` format: `YYYY-MM`
- Body: `multipart/form-data` with a CSV file

#### CSV format

```csv
iban,date,currency,category,amount
PL61109010140000071219812874,2026-01-10,PLN,FOOD,-10.50
```

#### Validation rules

Per row validation includes:

- IBAN format
- ISO-4217 currency code
- valid date format
- date must belong to imported month
- category must be non-blank and max 100 chars
- amount must be non-zero

Invalid rows do **not** fail the entire import – they are reported and skipped.

---

## ⏱ Import Status

```
GET /api/import/{yearMonth}/status
```

Possible states:

- `PROCESSING`
- `COMPLETED`
- `FAILED`

The response also contains:

- number of imported rows
- number of rejected rows

This allows clients to check whether statistics are ready.

---

## 📊 Statistics API

Statistics are currently calculated **on demand** using MongoDB aggregation pipelines.

```
GET /api/statistics/monthly
```

### Query parameters

- `date` – `YYYY-MM`
- `groupedBy` – enum value:
    - `CATEGORY`
    - `IBAN`
    - `SUMMARY`

Example:

```
GET /api/statistics/monthly?date=2026-01&groupedBy=CATEGORY
```

### Response

Aggregated totals per group (e.g. category or IBAN or SUMMARY).

---

## 🔄 Future Improvements (by design)

The current solution intentionally keeps aggregation **synchronous** and **on-demand**, which is perfectly acceptable
for the scope of this task.

However, the architecture explicitly allows future extension to:

- asynchronous aggregation
- background jobs triggered after import completion
- persisted statistics with readiness status
- event-driven processing

This was consciously left as a documented extension point rather than overengineering the initial solution.

---

## 🧪 Testing Strategy

- **Unit tests** for parsers, validators and pure logic
- **Integration tests** for:
    - CSV import flow
    - transaction persistence
    - statistics aggregation
    - security (JWT enforcement)

MongoDB is started via **Testcontainers** to closely match production behavior.


