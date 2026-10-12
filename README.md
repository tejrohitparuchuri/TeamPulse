# [TeamPulse](teampulsebyptr.web.app)

> Where every message counts, and every member is seen.

A team collaboration platform in the spirit of Slack — workspaces, channels, and real-time chat — built around one idea: **involvement should be visible, not guessed at.**

Every message, reply, reaction, and interaction quietly feeds a live **Engagement Score**. Each workspace shows its members ranked from most to least involved, with a visual progress bar that tells the story at a glance.

---

## 🔗 Related Repositories

- **Main Full-Stack Repository:** [tejrohitparuchuri/TeamPulse](https://github.com/tejrohitparuchuri/TeamPulse)
- **Backend Service Repository:** [tejrohitparuchuri/tt-3-1-backend-ext](https://github.com/tejrohitparuchuri/tt-3-1-backend-ext)
- **Frontend Client Repository:** [tejrohitparuchuri/tt-3-1-frontend-ext](https://github.com/tejrohitparuchuri/tt-3-1-frontend-ext)

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
- [Roadmap](#roadmap)
- [Author](#author)
- [License](#license)

---

## Features

| Feature | Description |
|---|---|
| **Workspaces** | Top-level team containers with join-codes and custom roles. Every workspace starts with a default `#general` channel. |
| **Channels** | Topic-based rooms inside a workspace, public or private. |
| **Real-time messaging** | Instant messaging with live polling and draft persistence. |
| **Engagement ranking** | Members ranked live by weighted activity with relative visual progress bars. |
| **Flexible Authentication** | Log in with either **Username** or **Email**, with real-time username availability validation. |
| **Role-based Controls** | Workspace owners can invite members, generate join-codes, create channels, and manage permissions. |
| **Pure Vanilla Frontend** | Zero heavy runtime framework dependencies — lightning-fast on-device rendering with modern dark mode aesthetics. |

---

## How Ranking Works

Every qualifying action generates an **Engagement Event** with a fixed weight:

| Engagement Event | Weight | Why it counts |
|---|---|---|
| Message sent | 2 pts | Core participation signal |
| Reply / thread response | 3 pts | Rewards conversation, not broadcasting |
| Reaction added | 1 pt | Acknowledgement of team content |
| Huddle / call joined | 5 pts | High-effort, synchronous presence |
| Active minutes online | 0.1 pt/min | Sustained presence without rewarding idle tabs |

The Ranking Engine folds each new event straight into the member's running score. Updates are **incremental** — only the affected member is re-scored, then the workspace list is re-sorted — so the leaderboard stays live without heavy database re-aggregations.

### Bar length

The top-scoring member always shows a **full bar**. Everyone else is drawn relative to them:

```
bar % = (member score ÷ top member score) × 100
```

### Bar colour

| Score (relative to #1) | Colour |
|---|---|
| ≥ 80% | Purple (`#8b5cf6`) |
| 65% – 79% | Green (`#10b981`) |
| 50% – 64% | Yellow (`#f59e0b`) |
| 35% – 49% | Orange (`#f97316`) |
| < 35% | Red (`#ef4444`) |

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend Framework** | Spring Boot 4.1.1 (Java 17+) |
| **Security** | Spring Security 6, BCrypt Password Encoding |
| **Build Tool** | Apache Maven & Maven Wrapper (`mvnw`) |
| **Database** | MySQL 8.x (InnoDB, `teampulse` schema) |
| **ORM / Persistence** | Spring Data JPA / Hibernate |
| **Frontend** | Pure Vanilla JavaScript, HTML5, Modern CSS3 |
| **Development Servers** | Local Spring Boot on port `3333`, Python HTTP Server on port `5500` |
| **Version Control** | Git & GitHub |

---

## Architecture

```
┌──────────────────────────────────────┐     ┌─────────────────────────────────────┐
│           Frontend Client            │     │         Spring Boot Backend         │
│   (Vanilla JS + CSS on Port 5500)    │     │            (Port 3333)              │
│                                      │     │                                     │
│  - index.html (Landing)              │     │  - WebConfig (CORS allowed)         │
│  - login.html (Username/Email Auth)  │────▶│  - SecurityConfig (PermitAll / CSRF)│
│  - register.html (Real-time check)   │ REST│  - AuthController                   │
│  - dashboard.html (Workspaces)       │ API │  - WorkspaceController              │
│  - workspace.html (Chat & Rankings)  │     │  - ChannelController                │
└──────────────────────────────────────┘     │  - MessageController                │
                                             │  - RankingService (Engagement Calc) │
                                             └──────────────────┬──────────────────┘
                                                                │
                                                                ▼ Spring Data JPA
                                                     ┌─────────────────────┐
                                                     │   MySQL Database    │
                                                     │ 127.0.0.1:3306/     │
                                                     │    teampulse        │
                                                     └─────────────────────┘
```

---

## Getting Started

### Prerequisites

- **Java 17** or newer installed (`java -version`)
- **MySQL 8.x** running locally on port `3306`
- **Python 3.x** (or any static HTTP file server) for serving the frontend

---

### 1. Clone the repository

```bash
git clone https://github.com/tejrohitparuchuri/TeamPulse.git
cd TeamPulse
```

---

### 2. Configure the MySQL Database

1. Log into your MySQL server:
   ```sql
   CREATE DATABASE teampulse;
   ```
2. Open `backend/tp/src/main/resources/application.properties` and verify your MySQL password:
   ```properties
   spring.datasource.url=jdbc:mysql://127.0.0.1:3306/teampulse
   spring.datasource.username=root
   spring.datasource.password=ptr3
   ```

---

### 3. Run the Backend Service

Navigate to the Spring Boot application directory and launch the server:

- **Windows:**
  ```powershell
  cd backend\tp
  .\mvnw.cmd spring-boot:run
  ```
- **macOS / Linux:**
  ```bash
  cd backend/tp
  ./mvnw spring-boot:run
  ```

The backend starts up on **`http://localhost:3333`**.

---

### 4. Run the Frontend Client

In a separate terminal, serve the `frontend/` directory on port `5500`:

```bash
cd frontend
python -m http.server 5500
```

Open your browser and navigate to:
👉 **[http://localhost:5500/login.html](http://localhost:5500/login.html)**

---

## Configuration

[`backend/tp/src/main/resources/application.properties`](file:///c:/Users/Acer/Desktop/tt-sem-end-project/backend/tp/src/main/resources/application.properties):

```properties
spring.application.name=teampulse
server.port=3333

spring.datasource.url=jdbc:mysql://127.0.0.1:3306/teampulse
spring.datasource.username=root
spring.datasource.password=ptr3

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## Project Structure

```
TeamPulse/
├── README.md
├── backend/
│   ├── TeamPulse_Project_Abstract.docx
│   └── tp/                                    # Spring Boot REST API
│       ├── pom.xml                            # Maven dependencies
│       ├── mvnw / mvnw.cmd                    # Maven wrapper
│       └── src/
│           ├── main/
│           │   ├── java/app/
│           │   │   ├── component/             # DataSeeder
│           │   │   ├── config/                # SecurityConfig, WebConfig (CORS)
│           │   │   ├── controller/            # Auth, Workspace, Channel, Message, etc.
│           │   │   ├── entity/                # User, Workspace, Channel, Message, Event
│           │   │   ├── repository/            # Spring Data JPA Repositories
│           │   │   └── service/               # RankingService
│           │   └── resources/
│           │       ├── application.properties # Server port & DB connection
│           │       └── banner.txt
│           └── test/
└── frontend/                                  # Pure Vanilla JS Frontend
    ├── index.html                             # Landing page
    ├── login.html                             # Login with Username / Email
    ├── register.html                          # User registration
    ├── dashboard.html                         # Workspace list & creation
    └── workspace.html                         # Channel chat & live engagement rankings
```

---

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Create a new user account |
| `POST` | `/api/auth/login` | Authenticate by username or email |
| `GET` | `/api/auth/check-username?username=...` | Check if username is available |
| `GET` | `/api/workspaces/user/{userId}` | List workspaces joined by user |
| `POST` | `/api/workspaces/user/{userId}` | Create a new workspace |
| `POST` | `/api/workspaces/{wid}/join` | Join workspace via join code |
| `GET` | `/api/workspaces/{id}/rankings` | Get ranked workspace members & scores |
| `GET` | `/api/workspaces/{id}/join-code?userId=...` | Get workspace join code (admin only) |
| `GET` | `/api/channels/workspace/{wid}?userId={uid}`| List channels in workspace |
| `POST` | `/api/channels/workspace/{wid}` | Create a new channel (admin only) |
| `GET` | `/api/messages/channel/{channelId}` | Fetch messages for a channel |
| `POST` | `/api/messages` | Send message (triggers engagement score update) |
| `GET` | `/api/drafts/channel/{cid}?userId={uid}` | Load saved unsent draft |
| `POST` | `/api/drafts` | Save draft in real time |
| `DELETE` | `/api/drafts/channel/{cid}?userId={uid}` | Clear draft after sending message |
| `GET` | `/api/users/{userId}` | Get user profile details |
| `POST` | `/api/users/{userId}/status` | Update online/offline user status |

---

## Data Model

| Entity | Purpose |
|---|---|
| `USER` | Account holder with email, username, and encrypted password. |
| `ROLE` | Workspace permission set (`ADMIN`, `MEMBER`). |
| `WORKSPACE` | Team collaboration container with an owner and unique join code. |
| `WORKSPACE_MEMBER` | Links User ↔ Workspace; stores `engagement_score` and `rank_position`. |
| `CHANNEL` | Topic discussion room inside a workspace. |
| `CHANNEL_MEMBER` | Links User ↔ Channel. |
| `MESSAGE` | Chat message linked to channel and author. |
| `DRAFT` | Temporary unsent message saved per user and channel. |
| `ENGAGEMENT_EVENT` | Append-only activity log ingested by `RankingService`. |

---

## Roadmap

- [ ] WebSocket STOMP real-time message broadcasting
- [ ] Direct one-on-one messaging
- [ ] File and media attachment uploads
- [ ] Customizable engagement weighting per workspace admin
- [ ] Historic analytics and engagement trend graphs

---

## Author

**Tej Rohit Paruchuri**  
Anurag University · Roll No. 24EG105R02  
[tejrohitparuchuri@gmail.com](mailto:tejrohitparuchuri@gmail.com) · [24eg105r02@anurag.edu.in](mailto:24eg105r02@anurag.edu.in)

---

## License

This project is licensed under the MIT License.
