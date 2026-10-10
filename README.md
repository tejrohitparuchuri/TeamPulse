# TeamPulse

> Where every message counts, and every member is seen.

A team collaboration platform in the spirit of Slack — workspaces, channels, and real-time chat — built around one idea: **involvement should be visible, not guessed at.**

Every message, reply, reaction, and huddle quietly feeds a live **Engagement Score**. Each workspace shows its members ranked from most to least involved, with a progress bar that tells the story at a glance.

---

## Table of Contents

- [Features](#features)
- [How Ranking Works](#how-ranking-works)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [API Overview](#api-overview)
- [Data Model](#data-model)
- [SEO Approach](#seo-approach)
- [Roadmap](#roadmap)
- [Author](#author)

---

## Features

| Feature | Description |
|---|---|
| **Workspaces** | Top-level team containers. Every workspace starts with a default `#general` channel. |
| **Channels** | Topic-based rooms inside a workspace, public or private. |
| **Real-time messaging** | Messages render on-device instantly, then sync over WebSocket. |
| **Engagement ranking** | Members ranked live by weighted activity, with relative progress bars. |
| **Authentication** | Stateless JWT login, BCrypt-hashed passwords. |
| **Authorization** | Role-aware access (`ADMIN` / `MEMBER`), scoped per workspace. |
| **SEO-ready public pages** | Server-rendered with Thymeleaf so crawlers see real HTML. |

---

## How Ranking Works

Every qualifying action becomes an **Engagement Event** with a fixed weight:

| Engagement Event | Weight | Why it counts |
|---|---|---|
| Message sent | 2 pts | Core participation signal |
| Reply / thread response | 3 pts | Rewards conversation, not broadcasting |
| Reaction added | 1 pt | Small but real acknowledgement |
| Huddle / call joined | 5 pts | High-effort, synchronous presence |
| Active minutes online | 0.1 pt/min | Sustained presence, without rewarding idle tabs |

The Ranking Engine folds each new event straight into the member's running score. Updates are **incremental** — only the affected member is re-scored, then the workspace list is re-sorted — so the leaderboard stays live without a full recalculation.

### Bar length

The top-scoring member always shows a **full bar**. Everyone else is drawn relative to them:

```
bar % = (member score ÷ top member score) × 100
```

### Bar colour

| Score (relative to #1) | Colour |
|---|---|
| ≥ 80% | Purple |
| 65% – 79% | Green |
| 50% – 64% | Yellow |
| 35% – 49% | Orange |
| < 35% | Red |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Build Tool | Apache Maven |
| Backend | Spring Boot — Web, Security, DevTools, Lombok |
| Public / SEO Pages | Thymeleaf (server-rendered HTML) |
| App Frontend | Vanilla JavaScript + Vanilla CSS (on-device rendering) |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL |
| Security | Spring Security, JWT, BCrypt |
| Real-time | WebSocket |
| Version Control | Git & GitHub |

No heavy frontend framework — interactions stay fast and the footprint stays small.

---

## Architecture

```
Public Pages          Authenticated App          WebSocket Client
(Thymeleaf SSR)       (Vanilla JS + CSS)         (live chat + ranking)
      |                       |                          |
      +-----------------------+--------------------------+
                              |
                  Spring Security Filter Chain
                (JWT Authentication & Authorization)
                              |
      +-----------------------+--------------------------+
      |                       |                          |
 Controller Layer        Service Layer             Repository Layer
 (REST + Views)      (Business Logic,             (Spring Data JPA)
                      Ranking Engine)
                              |
                       MySQL Database
```

---

## Getting Started

### Prerequisites

- Java 17 or newer
- Apache Maven
- MySQL 8.x running locally

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/teampulse.git
cd teampulse
```

### 2. Create the database

```sql
CREATE DATABASE teampulse;
```

### 3. Configure credentials

Update `src/main/resources/application.properties` (see [Configuration](#configuration)).

### 4. Build and run

```bash
mvn clean
mvn install -DskipTests
mvn spring-boot:run
```

The app starts at **http://localhost:8080**.

---

## Configuration

`src/main/resources/application.properties`

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/teampulse
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Thymeleaf
spring.thymeleaf.cache=false

# JWT
app.jwt.secret=YOUR_SECRET_KEY
app.jwt.expiration-ms=86400000
```

> **Note:** Never commit real credentials. Use environment variables or a local `application-dev.properties` that is listed in `.gitignore`.

---

## Project Structure

```
backend/
├── src/main/java/app/
│   ├── controller/     # REST endpoints + Thymeleaf views
│   ├── service/        # Business logic + Ranking Engine
│   ├── repository/     # Spring Data JPA interfaces
│   ├── entity/         # JPA entities mapped to tables
│   ├── dto/            # Data Transfer Objects
│   ├── config/         # Security & WebSocket configuration
│   └── security/       # JWT filter, token utility
├── src/main/resources/
│   ├── templates/      # Thymeleaf HTML templates
│   ├── static/
│   │   ├── css/        # Vanilla CSS
│   │   └── js/         # Vanilla JavaScript
│   └── application.properties
└── pom.xml
```

---

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Create a new account |
| `POST` | `/api/v1/auth/login` | Authenticate, receive JWT |
| `GET` | `/api/v1/workspaces` | List the user's workspaces |
| `POST` | `/api/v1/workspaces` | Create a workspace |
| `GET` | `/api/v1/workspaces/{id}/ranking` | Ranked members with scores |
| `GET` | `/api/v1/workspaces/{id}/channels` | List channels |
| `POST` | `/api/v1/channels` | Create a channel |
| `GET` | `/api/v1/channels/{id}/messages` | Fetch channel messages |
| `POST` | `/api/v1/channels/{id}/messages` | Post a message |

All endpoints except `/auth/**` require:

```
Authorization: Bearer <token>
```

---

## Data Model

| Entity | Purpose |
|---|---|
| `USER` | An account holder, independent of any one workspace |
| `ROLE` | Permission set (`ADMIN`, `MEMBER`) |
| `WORKSPACE` | A team container, owned by a user |
| `WORKSPACE_MEMBER` | Links user ↔ workspace; holds `engagement_score` and `rank_position` |
| `CHANNEL` | A topic room inside a workspace |
| `CHANNEL_MEMBER` | Links user ↔ channel |
| `MESSAGE` | A chat message, tied to its channel and sender |
| `ENGAGEMENT_EVENT` | Append-only activity log the Ranking Engine reads |

Engagement is kept as an **append-only log**, so the raw activity trail stays auditable, while `engagement_score` and `rank_position` act as a fast, ready-to-render cache for the UI.

---

## SEO Approach

Private workspaces have no business being indexed — but the public face of the platform absolutely should be found.

- Public pages (landing, sign-up, workspace previews) are **server-rendered with Thymeleaf**, arriving complete with titles, meta descriptions, and semantic markup.
- A maintained `sitemap.xml` and `robots.txt` tell crawlers exactly what to index.
- Fast first paint, since pages arrive fully formed rather than waiting on a framework to hydrate.

---

## Roadmap

- [ ] Configurable scoring weights per workspace admin
- [ ] Weekly / monthly ranking snapshots and trend charts
- [ ] Badges and milestones for sustained engagement
- [ ] Digest notifications when a member's engagement drops
- [ ] File sharing in channels
- [ ] Direct messages between members

---

## Author

**Tej Rohit Paruchuri**
Anurag University · Roll No. 24EG105R02
[tejrohitparuchuri@gmail.com](mailto:tejrohitparuchuri@gmail.com) · [24eg105r02@anurag.edu.in](mailto:24eg105r02@anurag.edu.in)

---

## License

This project is available under the MIT License.
