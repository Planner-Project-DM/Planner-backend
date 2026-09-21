# ✈️ Travel Planner API

> REST API for group travel planning with real-time notifications, friendships, and budgeting.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black?style=flat-square)
![Coverage](https://img.shields.io/badge/coverage-70%25-green?style=flat-square)
![CI](https://img.shields.io/badge/CI-passing-brightgreen?style=flat-square)

---

## 📋 Table of contents

- [About](#about)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Local setup](#local-setup)
- [API documentation](#api-documentation)
- [Tests](#tests)
- [CI/CD](#cicd)
- [Monitoring](#monitoring)
- [Project structure](#project-structure)

---

## 🧭 About

Travel Planner API is the backend for a group travel planning application. Users can create trips, invite friends to groups, build day-by-day schedules and itineraries, track budgets, and receive real-time notifications via WebSockets.

This project was created as a portfolio piece demonstrating production-oriented practices: DB migrations with Flyway, integration tests with Testcontainers, monitoring with Prometheus/Grafana, and deployment via CI/CD to a VPS.

---

## 🛠 Tech stack

### Backend

| Layer | Technology | Why |
|---|---|---|
| Framework | Spring Boot 4.1.0 + Java 21 | Virtual threads, modern ecosystem |
| Database | PostgreSQL 16 | ACID, maturity, JSON support |
| DB migrations | Flyway | Schema versioning and reproducible environments |
| ORM | Spring Data JPA + Hibernate | `@EntityGraph` to avoid N+1 |
| Rate limiting cache | Caffeine | In-memory, per-IP counters for low overhead |
| Data cache | Redis + `@Cacheable` | Shared cache across instances |
| Messaging | Apache Kafka + DLT | Asynchronous events, consumer error resilience |
| Real-time | WebSockets (STOMP) | Push notifications to frontend |
| Security | Spring Security + JWT | Stateless authentication |
| Docs | SpringDoc OpenAPI 3 | Auto-generated Swagger UI |
| Scheduler | Spring Scheduler | Reminders and scheduled jobs |
| Monitoring | Actuator + Micrometer + Prometheus | Production and business metrics |
| Build | Gradle (Kotlin DSL) | Faster, modern syntax |

### Infrastructure

| Tool | Purpose |
|---|---|
| Docker + Docker Compose | Containerization for local development |
| VPS (Ubuntu) | Production hosting |
| GitHub Actions | CI/CD pipelines |
| Grafana | Prometheus metrics visualization |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        React Frontend                           │
└──────────────────────┬──────────────────────────────────────────┘
                       │ REST / WebSocket (STOMP)
┌──────────────────────▼──────────────────────────────────────────┐
│                     Spring Boot API                             │
│                                                                 │
│  ┌───────────┐  ┌───────────┐  ┌────────────┐  ┌────────────┐   │
│  │Auth (JWT) │  │   Trip    │  │  Schedule  │  │ Settings   │   │
│  └───────────┘  └───────────┘  └────────────┘  └────────────┘   │
│  ┌───────────┐  ┌───────────┐  ┌────────────┐  ┌────────────┐   │
│  │  Groups   │  │Friendship │  │Notification│  │  Reports   │   │
│  └───────────┘  └───────────┘  └────────────┘  └────────────┘   │
└────────┬──────────────┬──────────────┬───────────────┬──────────┘
         │              │              │               │
┌────────▼────┐  ┌──────▼──────┐  ┌────▼─────┐  ┌──────▼──────┐
│ PostgreSQL  │  │    Kafka    │  │ Caffeine │  │    Redis    │
│  + Flyway   │  │    + DLT    │  │  (rate)  │  │   (cache)   │
└──────┬──────┘  └─────────────┘  └──────────┘  └─────────────┘
       │
┌──────▼──────────────────┐
│  Prometheus + Grafana   │
│  Spring Actuator        │
└─────────────────────────┘
```

### Architectural patterns

- Layered architecture — Controller → Service → Repository
- DTO pattern — separate domain model from API
- Domain events — trip and friendship changes published to Kafka and consumed by the notification module
- `@EntityGraph` — avoid N+1 when loading relations (trip → members, schedule, itinerary)
- Cache per purpose — Caffeine for rate limiting, Redis for shared entity cache

---

## ✨ Features

### Auth
- Registration and login with JWT

### Trips
- CRUD for trips
- Trip schedules — add, update, delete days
- Itinerary — detailed items inside schedules

### Groups
- Add/remove members from trip groups
- WebSocket notifications on group changes

### Friendships
- Send friend requests
- Accept/reject/block requests
- List friends and pending requests

### Notifications
- Fetch unread notifications
- Mark single/all notifications as read
- Real-time delivery via Kafka → WebSocket

### Weather
- Current weather for destination city

### Reports & Budget
- Export trip budget summary to Excel (.xlsx)

### User settings
- Get and update per-user settings (language, currency, preferences)
- Settings cached in Redis (`@Cacheable`)

---

## 🚀 Local setup

### Requirements

- Java 21
- Docker + Docker Compose
- Gradle 8+

### Quick start

```bash
# 1. Clone repository
git clone https://github.com/Planner-Project-DM/Planner-backend.git
cd Planner-backend

# 2. Start infrastructure (PostgreSQL + Kafka + Redis + Prometheus + Grafana)
# A sample docker-compose.yml is included in the repository for local development:
docker compose up -d

# 3. Run the application
./gradlew bootRun

# 4. Check health
curl http://localhost:8080/actuator/health
```

The app runs at `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Services started by docker-compose

```
PostgreSQL 16    → localhost:5432
Apache Kafka     → localhost:9092
Redis            → localhost:6379
Prometheus       → localhost:9090
Grafana          → localhost:3000  (admin/admin)
```

### Environment variables

Copy and fill the example:

```bash
cp .env.example .env
```

Fill values in `.env` according to the table below.

| Variable | Description | Example |
|---|---|---|
| `DATABASE_URL` | JDBC URL to PostgreSQL | `jdbc:postgresql://localhost:5432/planner` |
| `DATABASE_USERNAME` | DB user | `planner` |
| `DATABASE_PASSWORD` | DB password | `secret` |
| `JWT_SECRET_KEY` | JWT signing key (min. 32 chars) | `your-256-bit-secret` |
| `JWT_EXPIRATION` | Access token TTL (ms) | `900000` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |

---

## 📖 API documentation

### Endpoints

#### Auth
```
POST   /auth/register                         Register a new user
POST   /auth/login                            Login, returns JWT
```

#### Trips
```
GET    /api/trips                             List current user's trips
POST   /api/trips                             Create a new trip
GET    /api/trips/{id}                        Get trip details
PATCH  /api/trips/{id}                        Update a trip
DELETE /api/trips/{id}                        Delete a trip
```

#### Trip Schedule
```
GET    /api/trips/{id}/schedules              Get trip schedule
POST   /api/trips/{id}/schedules              Add a day to schedule
PUT    /api/trips/{id}/schedules              Update schedule
DELETE /api/trips/{id}/schedules              Delete a schedule day
```

#### Trip Itinerary
```
POST   /api/trips/{id}/item                   Add an itinerary item
PUT    /api/trips/{id}/item                   Update an item
DELETE /api/trips/{id}/item                   Delete an item
```

#### Groups
```
POST   /api/trips/{id}/group/members          Add a member to the group
PUT    /api/trips/{id}/group/members          Update a group member
DELETE /api/trips/{id}/group/members          Remove a member from the group
```

#### Friendships
```
GET    /api/friendships                       List friends
POST   /api/friendships/create                Send friend request
GET    /api/friendships/my-requests           My pending requests
PATCH  /api/friendships/{id}/accept           Accept a request
PATCH  /api/friendships/{id}/reject           Reject a request
PATCH  /api/friendships/{id}/block            Block a user
DELETE /api/friendships                       Remove friend
```

#### Notifications
```
GET    /api/notifications/unread              Unread notifications
POST   /api/notifications/{id}/markAsRead     Mark as read
POST   /api/notifications/markAllAsRead       Mark all as read
```

#### Trip Items (search)
```
GET    /api/trip-items/city/{city}            Attractions/items for a city
```

#### Reports
```
GET    /api/reports/{id}/funds-summary        Export trip budget summary to Excel
```

#### User Settings
```
GET    /api/users/settings                    Get user settings
PUT    /api/users/settings                    Update user settings
```

### Example requests

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{ "email": "jan@example.com", "password": "SecurePass123!" }'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "jan@example.com", "password": "SecurePass123!" }'

# Create trip (with JWT)
curl -X POST http://localhost:8080/api/trips \
  -H "Authorization: ******" \
  -H "Content-Type: application/json" \
  -d '{ "name": "Greece Vacation", "startDate": "2026-07-01", "endDate": "2026-07-14" }'

# Export budget to Excel
curl -X GET http://localhost:8080/api/reports/1/funds-summary \
  -H "Authorization: ******" \
  --output budget.xlsx
```

---

## 🧪 Tests

The project enforces a minimum of **70% code coverage** (verified by JaCoCo in CI).

### Test types

| Type | Tools | What it tests |
|---|---|---|
| Unit | JUnit 5 + Mockito | Service logic in isolation |
| Controller | MockMvc + `@WebMvcTest` | Endpoints, request validation, HTTP codes |
| Repository | `@DataJpaTest` | JPA queries and custom repo methods |
| Integration | Testcontainers + `@SpringBootTest` | Real PostgreSQL + Kafka in containers |

### Run tests

```bash
# All tests
./gradlew test

# JaCoCo report
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

---

## ⚙️ CI/CD

A GitHub Actions workflow runs build & test, security scan, image build/push, and deploy to VPS.

### Workflow jobs

| Job | Trigger | Purpose |
|---|---|---|
| `build-and-test` | push / PR → `main` | Build, tests, JaCoCo, publish reports |
| `security-scan` | push / PR → `main` | Vulnerability scan with Trivy |
| `build-and-push-docker` | after previous jobs (on push) | Build Docker image and push to GHCR |
| `deploy-vps` | after image push (on push) | SSH to VPS and run `docker compose pull` + `docker compose up -d` |

PRs to `main` run the first two jobs only (no image push).

### Required secrets

| Secret | Description |
|---|---|
| `DATABASE_TEST_URL` | JDBC URL for test DB on VPS |
| `DATABASE_TEST_USERNAME` | Test DB user |
| `DATABASE_TEST_PASSWORD` | Test DB password |
| `VPS_SSH_HOST` | VPS IP / domain |
| `VPS_SSH_USER` | SSH user |
| `VPS_SSH_KEY` | SSH private key |
| `VPS_SSH_PORT` | SSH port (default 22) |
| `GITHUB_TOKEN` | GitHub token used for GHCR & deployment |

---

## 📊 Monitoring

### Spring Actuator

```
GET /actuator/health      Application, PostgreSQL, Kafka, Redis health
GET /actuator/info        App version, Java
GET /actuator/metrics     List of metrics
GET /actuator/prometheus  Prometheus scrape endpoint
GET /actuator/loggers     Change logging levels at runtime
```

### Custom business metrics

| Metric | Type | Description |
|---|---|---|
| `planner_trip_created_total` | Counter | Number of created trips (tag: `module=trip`) |
| `planner_auth_login_duration_seconds` | Timer | Login duration — p50, p95, p99 |
| `planner_websocket_active_connections` | Gauge | Active WebSocket connections |

### Grafana

Grafana available at `http://localhost:3000` (admin/admin) when running locally. Import Spring Boot dashboard ID **12900**.

---

## 📁 Project structure

```
src/main/java/net/dysky/planner/
├── auth/                   # JWT, registration, login
├── user/                   # User entity
├── usersettings/           # Per-user settings, Redis cache
├── trip/                   # Trip CRUD
├── tripschedule/           # Trip schedule (days)
├── tripitinerary/          # Itinerary items per day
├── tripitem/               # City attractions / items
├── group/                  # Trip groups, members
├── friendship/             # Friendship system (accept/reject/block)
├── notification/           # WebSocket + Kafka notifications
├── weather/                # Weather API integration
├── report/                 # Budget export to Excel (.xlsx)
└── config/                 # SecurityConfig, KafkaConfig,
                            # CaffeineConfig, RedisConfig

src/main/resources/
├── db/migration/           # Flyway migrations (V1__initial_schema.sql present)
├── application.properties

src/test/java/net/dysky/planner/
├── auth/                   # Auth tests
├── exception/              # Exception handling tests
├── friendship/             # Friendship tests
├── group/                  # Group tests
├── notification/           # Notification tests (Testcontainers Kafka)
├── schedule/               # Schedule tests (Testcontainers)
├── trip/                   # Trip tests
├── tripSchedule/           # Trip schedule tests
├── tripitem/               # Trip item tests
├── tripitinerary/          # Itinerary tests
├── user/                   # User tests
├── usersettings/           # User settings tests
├── weather/                # Weather integration tests
└── AbstractIntegrationTest.java  # Base class for Testcontainers (Postgres + Kafka)
```

---

## 📄 License

MIT License.

---

*This backend works with a React frontend (separate repository).*
