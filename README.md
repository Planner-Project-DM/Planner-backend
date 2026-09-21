# ✈️ Travel Planner API

> REST API planera podróży grupowych z powiadomieniami w czasie rzeczywistym, systemem znajomości i budżetowaniem.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?style=flat-square)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-black?style=flat-square)
![Coverage](https://img.shields.io/badge/coverage-70%25-green?style=flat-square)
![CI](https://img.shields.io/badge/CI-passing-brightgreen?style=flat-square)

---

## 📋 Spis treści

- [O projekcie](#-o-projekcie)
- [Stack technologiczny](#-stack-technologiczny)
- [Architektura](#-architektura)
- [Funkcjonalności](#-funkcjonalności)
- [Uruchomienie lokalne](#-uruchomienie-lokalne)
- [Dokumentacja API](#-dokumentacja-api)
- [Testy](#-testy)
- [CI/CD](#-cicd)
- [Monitoring](#-monitoring)
- [Struktura projektu](#-struktura-projektu)

---

## 🧭 O projekcie

Travel Planner API to backend aplikacji do planowania podróży grupowych. Użytkownicy mogą tworzyć podróże, zapraszać znajomych do grup, budować harmonogram i itinerary dzień po dniu, śledzić budżet oraz otrzymywać powiadomienia w czasie rzeczywistym przez WebSockety.

Projekt powstał jako portfolio demonstrując produkcyjne podejście do budowy REST API — od migracji bazy danych przez testy integracyjne z Testcontainers, po monitoring przez Prometheus i Grafanę oraz wdrożenie na VPS z pełnym CI/CD.

---

## 🛠 Stack technologiczny

### Backend

| Warstwa | Technologia | Powód wyboru |
|---|---|---|
| Framework | Spring Boot 4.1.0 + Java 21 | Virtual threads, nowoczesny ekosystem |
| Baza danych | PostgreSQL 16 | ACID, dojrzałość, wsparcie JSON |
| Migracje DB | Flyway | Wersjonowanie schematu, powtarzalność środowisk |
| ORM | Spring Data JPA + Hibernate | `@EntityGraph` dla eliminacji N+1 |
| Cache (rate limiting) | Caffeine | In-memory, liczniki per IP — zerowy narzut sieciowy |
| Cache (dane) | Redis + `@Cacheable` | Cache encji współdzielony między instancjami |
| Messaging | Apache Kafka + DLT | Asynchroniczne eventy, odporność na błędy konsumera |
| Real-time | WebSockets (STOMP) | Powiadomienia push do frontendu |
| Security | Spring Security + JWT | Bezstanowe uwierzytelnianie |
| Dokumentacja | SpringDoc OpenAPI 3 | Auto-generowany Swagger UI |
| Scheduler | Spring Scheduler | Przypomnienia, zaplanowane zadania |
| Monitoring | Actuator + Micrometer + Prometheus | Metryki produkcyjne i biznesowe |
| Build | Gradle (Kotlin DSL) | Szybszy od Maven, nowoczesna składnia |

### Infrastruktura

| Narzędzie | Zastosowanie |
|---|---|
| Docker + Docker Compose | Konteneryzacja, lokalny development |
| VPS (Ubuntu) | Hosting produkcyjny |
| GitHub Actions | CI/CD (4 niezależne pipeline'y) |
| Grafana | Wizualizacja metryk Prometheus |

---

## 🏗 Architektura

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

### Wzorce architektoniczne

- **Layered architecture** — Controller → Service → Repository
- **DTO pattern** — oddzielenie modelu domenowego od API
- **Domain events** — zmiany w tripach i znajomościach publikowane na Kafkę, konsumowane przez moduł powiadomień
- **`@EntityGraph`** — eliminacja problemu N+1 przy zapytaniach z relacjami (trip → members, schedule, itinerary)
- **Cache per zastosowanie** — Caffeine do rate limitingu (in-memory, per-instance), Redis do cache encji (współdzielony)

---

## ✨ Funkcjonalności

### Autoryzacja
- Rejestracja i logowanie z JWT

### Podróże (Trip)
- CRUD podróży
- Harmonogram podróży (schedules) — dodawanie, edycja, usuwanie dni
- Itinerary — szczegółowe pozycje (items) w ramach harmonogramu

### Grupy
- Dodawanie i usuwanie członków grupy podróżnej
- Powiadomienia WebSocket przy zmianach w grupie

### Znajomości (Friendship)
- Wysyłanie zaproszeń do znajomych
- Akceptowanie, odrzucanie, blokowanie zaproszeń
- Przeglądanie znajomych i oczekujących zaproszeń

### Powiadomienia
- Pobieranie nieprzeczytanych powiadomień
- Oznaczanie jako przeczytane (pojedynczo i wszystkie)
- Dostarczanie przez WebSocket w czasie rzeczywistym (Kafka → WebSocket)

### Raporty i budżet
- Eksport podsumowania budżetu podróży (funds summary) do Excel (.xlsx)

### Ustawienia użytkownika
- Pobieranie i aktualizacja ustawień per-user (język, waluta, styl podróży, preferencje powiadomień)
- Cache ustawień w Redis (`@Cacheable`)

---

## 🚀 Uruchomienie lokalne

### Wymagania

- Java 21
- Docker + Docker Compose
- Gradle 8+

### Krok po kroku

```bash
# 1. Sklonuj repozytorium
git clone https://github.com/Planner-Project-DM/Planner-backend.git
cd Planner-backend

# 2. Uruchom infrastrukturę (PostgreSQL + Kafka + Redis + Prometheus + Grafana)
docker compose up -d

# 3. Uruchom aplikację
./gradlew bootRun

# 4. Sprawdź czy działa
curl http://localhost:8080/actuator/health
```

Aplikacja startuje na `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Docker Compose — co się uruchamia

```
PostgreSQL 16    → localhost:5432
Apache Kafka     → localhost:9092
Redis            → localhost:6379
Prometheus       → localhost:9090
Grafana          → localhost:3000  (admin/admin)
```


### Zmienne środowiskowe

Skopiuj i uzupełnij:

```bash
cp .env.example .env
```

Uzupełnij wartości w .env zgodnie z opisem w tabeli zmiennych środowiskowych poniżej.


| Zmienna | Opis | Przykład |
|---|---|---|
| `DB_URL` | JDBC URL do PostgreSQL | `jdbc:postgresql://localhost:5432/planner` |
| `DB_USERNAME` | Użytkownik bazy | `planner` |
| `DB_PASSWORD` | Hasło bazy | `secret` |
| `JWT_SECRET` | Klucz do podpisywania tokenów (min. 32 znaki) | `your-256-bit-secret` |
| `JWT_EXPIRATION` | Czas życia access tokena (ms) | `900000` |
| `KAFKA_BOOTSTRAP` | Adres Kafka brokerów | `localhost:9092` |
| `REDIS_HOST` | Host Redis | `localhost` |
| `REDIS_PORT` | Port Redis | `6379` |

---

## 📖 Dokumentacja API

### Endpointy

#### Auth
```
POST   /auth/register                         Rejestracja nowego użytkownika
POST   /auth/login                            Logowanie, zwraca JWT
```

#### Trips
```
GET    /api/trips                             Lista podróży zalogowanego użytkownika
POST   /api/trips                             Stworzenie nowej podróży
GET    /api/trips/{id}                        Szczegóły podróży
PATCH  /api/trips/{id}                        Aktualizacja podróży
DELETE /api/trips/{id}                        Usunięcie podróży
```

#### Trip Schedule
```
GET    /api/trips/{id}/schedules              Pobierz harmonogram podróży
POST   /api/trips/{id}/schedules              Dodaj dzień do harmonogramu
PUT    /api/trips/{id}/schedules              Zaktualizuj harmonogram
DELETE /api/trips/{id}/schedules              Usuń dzień z harmonogramu
```

#### Trip Itinerary
```
POST   /api/trips/{id}/item                   Dodaj pozycję do itinerary
PUT    /api/trips/{id}/item                   Zaktualizuj pozycję
DELETE /api/trips/{id}/item                   Usuń pozycję
```

#### Groups
```
POST   /api/trips/{id}/group/members          Dodaj członka do grupy
PUT    /api/trips/{id}/group/members          Zaktualizuj członka grupy
DELETE /api/trips/{id}/group/members          Usuń członka z grupy
```

#### Friendships
```
GET    /api/friendships                       Lista znajomych
POST   /api/friendships/create                Wyślij zaproszenie
GET    /api/friendships/my-requests           Moje oczekujące zaproszenia
PATCH  /api/friendships/{id}/accept           Akceptuj zaproszenie
PATCH  /api/friendships/{id}/reject           Odrzuć zaproszenie
PATCH  /api/friendships/{id}/block            Zablokuj użytkownika
DELETE /api/friendships                       Usuń znajomego
```

#### Notifications
```
GET    /api/notifications/unread              Nieprzeczytane powiadomienia
POST   /api/notifications/{id}/markAsRead     Oznacz jako przeczytane
POST   /api/notifications/markAllAsRead       Oznacz wszystkie jako przeczytane
```

#### Trip Items (wyszukiwanie)
```
GET    /api/trip-items/city/{city}            Atrakcje / pozycje dla danego miasta
```

#### Reports
```
GET    /api/reports/{id}/funds-summary        Eksport podsumowania budżetu do Excel
```

#### User Settings
```
GET    /api/users/settings                    Pobierz ustawienia użytkownika
PUT    /api/users/settings                    Zaktualizuj ustawienia
```

### Przykładowe requesty

```bash
# Rejestracja
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{ "email": "jan@example.com", "password": "SecurePass123!" }'

# Logowanie
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "jan@example.com", "password": "SecurePass123!" }'

# Tworzenie podróży (z JWT)
curl -X POST http://localhost:8080/api/trips \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{ "name": "Wakacje w Grecji", "startDate": "2026-07-01", "endDate": "2026-07-14" }'

# Eksport budżetu do Excel
curl -X GET http://localhost:8080/api/reports/1/funds-summary \
  -H "Authorization: Bearer <token>" \
  --output budget.xlsx
```

---

## 🧪 Testy

Projekt utrzymuje **minimum 70% pokrycia kodu** (wymuszane przez JaCoCo w CI).

### Rodzaje testów

| Rodzaj | Narzędzia | Co testuje |
|---|---|---|
| Unit | JUnit 5 + Mockito | Logika serwisów w izolacji |
| Controller | MockMvc + `@WebMvcTest` | Endpointy, walidacja requestów, kody HTTP |
| Repository | `@DataJpaTest` | Zapytania JPA, własne metody repo |
| Integracyjne | Testcontainers + `@SpringBootTest` | Prawdziwy PostgreSQL + Kafka |

### Uruchomienie testów

```bash
# Wszystkie testy
./gradlew test

# Raport JaCoCo
./gradlew jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### Testcontainers — testy integracyjne

```java
@SpringBootTest
@Testcontainers
class TripServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
    );
}
```

---

## ⚙️ CI/CD

Projekt ma jeden pipeline (`java-ci.yml`) z **4 jobami** uruchamianymi sekwencyjnie przy każdym push/PR do `main`:

| Job | Trigger | Co robi |
|---|---|---|
| `build-and-test` | push / PR → `main` | Build Gradle, testy, JaCoCo report, publikacja wyników jako artefakt |
| `security-scan` | push / PR → `main` | Skan podatności Trivy (CRITICAL + HIGH) na kodzie i zależnościach |
| `build-and-push-docker` | po `build-and-test` + `security-scan` (tylko push) | Buduje Docker image i pushuje do GHCR (`ghcr.io`) z tagiem `latest` i SHA |
| `deploy-vps` | po `build-and-push-docker` (tylko push) | SSH na VPS → `docker compose pull` + `docker compose up -d` |

### Kolejność wykonania

```
push → main
    ├── build-and-test    ─┐
    └── security-scan     ─┴─► build-and-push-docker ──► deploy-vps
```

PR do `main` uruchamia tylko pierwsze dwa joby (build + security) — image nie jest pushowany dopóki kod nie trafi do `main`.

### Sekrety wymagane w repo

| Sekret | Opis |
|---|---|
| `DATABASE_TEST_URL` | JDBC URL do bazy testowej na VPS |
| `DATABASE_TEST_USERNAME` | Użytkownik bazy testowej |
| `DATABASE_TEST_PASSWORD` | Hasło bazy testowej |
| `VPS_SSH_HOST` | Adres IP / domena VPS |
| `VPS_SSH_USER` | Użytkownik SSH |
| `VPS_SSH_KEY` | Klucz prywatny SSH |
| `VPS_SSH_PORT` | Port SSH (domyślnie 22) |
| `GITHUB_TOKEN` | Auto-generowany przez GitHub (GHCR + deploy) |

---

## 📊 Monitoring

### Spring Actuator

```
GET /actuator/health      Status aplikacji, PostgreSQL, Kafki, Redis
GET /actuator/info        Wersja aplikacji, Java
GET /actuator/metrics     Lista wszystkich metryk
GET /actuator/prometheus  Eksport metryk dla Prometheus
GET /actuator/loggers     Zmiana poziomów logowania na żywo
```

### Własne metryki biznesowe

| Metryka | Typ | Opis |
|---|---|---|
| `planner_trip_created_total` | Counter | Liczba stworzonych podróży (tag: `module=trip`) |
| `planner_auth_login_duration_seconds` | Timer | Czas logowania — p50, p95, p99 |
| `planner_websocket_active_connections` | Gauge | Aktywne połączenia WebSocket |

### Grafana

Po uruchomieniu lokalnym Grafana dostępna pod `http://localhost:3000` (admin/admin).  
Zaimportuj gotowy dashboard Spring Boot: **ID 12900**.

---

## 📁 Struktura projektu

```
src/main/java/net/dysky/planner/
├── auth/                   # JWT, rejestracja, logowanie
├── user/                   # Encja użytkownika
├── usersettings/           # Ustawienia per-user, cache Redis
├── trip/                   # CRUD podróży
├── tripschedule/           # Harmonogram podróży (dni)
├── tripitinerary/          # Pozycje itinerary w ramach dnia
├── tripitem/               # Atrakcje / pozycje dla miast
├── group/                  # Grupy podróżne, członkowie
├── friendship/             # System znajomości (accept/reject/block)
├── notification/           # Powiadomienia WebSocket + Kafka
├── weather/                # Integracja z Weather API
├── report/                 # Eksport budżetu do Excel (.xlsx)
└── config/                 # SecurityConfig, KafkaConfig,
                            # CaffeineConfig, RedisConfig

src/main/resources/
├── db/migration/           # Flyway migrations (V1__initial_schema.sql present)
├── application.properties

src/test/java/net/dysky/planner/
├── auth/                   # Testy auth (metryki, logowanie)
├── exception/              # Testy obsługi wyjątków
├── friendship/             # Testy systemu znajomości
├── group/                  # Testy grup podróżnych
├── notification/           # Testy powiadomień (Testcontainers Kafka)
├── schedule/               # Testy harmonogramu (Testcontainers)
├── trip/                   # Testy podróży (metryki)
├── tripSchedule/           # Testy trip schedule
├── tripitem/               # Testy trip items (Testcontainers)
├── tripitinerary/          # Testy itinerary
├── user/                   # Testy użytkownika (Testcontainers)
├── usersettings/           # Testy ustawień (cache Redis)
├── weather/                # Testy integracji z Weather API
└── AbstractIntegrationTest.java  # Bazowa klasa z Testcontainers (PostgreSQL + Kafka)
```

---

## 📄 Licencja

MIT License.

---

*Backend współpracuje z frontendem w React (osobne repozytorium).*