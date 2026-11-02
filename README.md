# 💬 Chat App

[![CI](https://github.com/vladikv/Chat-App/actions/workflows/ci.yml/badge.svg)](https://github.com/vladikv/Chat-App/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-green?style=flat-square&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-ready-blue?style=flat-square&logo=docker)

---

## ✨ Features

- 🔐 JWT Authentication (register / login)
- 💬 Real-time messaging via WebSocket + STOMP
- 🏠 Multiple chat rooms with rename support (creator only)
- 👥 Online/offline members list with live status updates
- ✏️ Edit & delete messages in real-time
- ⌨️ Typing indicator
- 🌙 Dark / Light theme toggle
- 👤 User profile page (edit username, email, password)
- 📱 Mobile-friendly with responsive layout
- 🐳 Docker + Docker Compose support
A full-featured real-time chat application built with **Java Spring Boot** and **WebSocket**, featuring a modern responsive frontend.

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 3.5 | Application framework |
| Spring Security | 6.x | JWT authentication |
| Spring WebSocket + STOMP | 6.x | Real-time messaging |
| Spring Data JPA / Hibernate | 6.x | ORM / database access |
| PostgreSQL | 17 | Primary database |
| Redis | 7 | Online users, unread counts |
| Bucket4j | 8.0.1 | Rate limiting |
| Springdoc OpenAPI | 2.5.0 | Swagger UI |
| Maven | 3.x | Build tool |

### Frontend
| Technology | Purpose |
|------------|---------|
| HTML / CSS / JavaScript | UI |
| SockJS + STOMP.js | WebSocket client |
| Web Audio API | Notification sounds |

### DevOps
| Technology | Purpose |
|------------|---------|
| Docker | Multi-stage build, non-root runtime image |
| Docker Compose | Multi-container orchestration (health checks, volumes) |
| GitHub Actions | CI: build and run tests on every push / PR |

---

## ✅ Implemented Features

### Authentication
- JWT registration and login
- Refresh token (access: 15min, refresh: 7 days)
- Token rotation on refresh
- Logout with token revocation
- Rate limiting: 5 req/min for auth endpoints

### Chat Rooms
- Create, rename (creator only), delete (creator only)
- Real-time messaging via WebSocket + STOMP
- Message history with infinite scroll (cursor-based pagination, 30 per page)
- Typing indicator
- Unread message badges
- Pin messages (one per room)
- Search messages within a room
- Date separators between messages

### Direct Messages (DMs)
- Private messaging between users
- Real-time delivery
- Reply support
- Unread badges
- Online indicator in DM list

### Messages
- Send, edit, delete (own messages only)
- Reply to messages (with quote preview)
- Reactions: emoji picker + quick react buttons
- Toggle reaction (click again to remove)
- File and image uploads (max 10MB)
  - Image preview before sending
  - Caption support
  - File type: image/*, PDF, DOC, DOCX, ZIP, TXT
- @mention with autocomplete dropdown
- Custom user tags for mentions
- Pin/unpin messages

### Users & Profiles
- Avatar: generated (initial + color) or uploaded photo
- Edit username, email, password, tag, avatar
- Online/offline status (powered by Redis with TTL)
- Members sidebar with online/offline lists

### UI/UX
- Dark / Light theme (persisted in localStorage)
- Responsive design — tested on iPhone 13
- Mobile swipe navigation (right to open sidebar)
- Settings menu (theme, sound, logout)
- Notification sound (Web Audio API, toggleable)
- Context menu (right-click / long-press on mobile)
- Bottom sheet for mobile room actions
- Emoji picker for reactions
- Search bar with message highlighting
- File upload preview modal
- Animated message appearance
- Separate card-style layout for sidebar, chat, members

### Technical
- WebSocket auto-reconnect after server restart
- Rate limiting per IP (auth) and per user (API), with `429` + `Retry-After` and an on-screen message
- Redis-backed online user tracking with TTL auto-expiry
- Refresh token storage in PostgreSQL
- Swagger UI at `/swagger-ui.html`
- File uploads served statically from `/uploads/`

---

## 🗂 Project Structure

```
src/
├── main/
│   ├── java/com/example/chat_app/
│   │   ├── config/
│   │   │   ├── JwtService.java
│   │   │   ├── JwtAuthFilter.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebSocketConfig.java
│   │   │   ├── WebSocketEventListener.java
│   │   │   ├── RedisConfig.java
│   │   │   ├── RateLimitFilter.java
│   │   │   ├── SwaggerConfig.java
│   │   │   └── WebMvcConfig.java
│   │   ├── controllers/
│   │   │   ├── AuthController.java
│   │   │   ├── ChatController.java
│   │   │   ├── ChatRoomController.java
│   │   │   ├── MessageController.java
│   │   │   ├── DirectMessageController.java
│   │   │   ├── UserController.java
│   │   │   ├── OnlineUserController.java
│   │   │   ├── FileController.java
│   │   │   └── UnreadController.java
│   │   ├── dto/
│   │   │   ├── account/
│   │   │   ├── chatroom/
│   │   │   ├── message/
│   │   │   └── dm/
│   │   ├── entities/
│   │   │   ├── UserEntity.java
│   │   │   ├── ChatRoomEntity.java
│   │   │   ├── MessageEntity.java
│   │   │   ├── DirectMessageEntity.java
│   │   │   ├── ReactionEntity.java
│   │   │   └── RefreshTokenEntity.java
│   │   ├── repositories/
│   │   │   ├── UserRepository.java
│   │   │   ├── ChatRoomRepository.java
│   │   │   ├── MessageRepository.java
│   │   │   ├── DirectMessageRepository.java
│   │   │   ├── ReactionRepository.java
│   │   │   └── RefreshTokenRepository.java
│   │   └── services/
│   │       ├── UserService.java
│   │       ├── ChatRoomService.java
│   │       ├── MessageService.java
│   │       ├── DirectMessageService.java
│   │       ├── OnlineUserService.java
│   │       ├── RefreshTokenService.java
│   │       └── UnreadService.java
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html
│           ├── style.css
│           ├── app.js
│           ├── login/
│           │   ├── login.html
│           │   ├── login.css
│           │   └── login.js
│           └── profile/
│               ├── profile.html
│               ├── profile.css
│               └── profile.js
└── test/
    ├── java/com/example/chat_app/
    │   ├── config/
    │   │   └── JwtServiceTest.java
    │   ├── controllers/
    │   │   └── AuthControllerIntegrationTest.java
    │   ├── ChatAppApplicationTests.java
    │   └── services/
    │       ├── ChatRoomServiceTest.java
    │       ├── DirectMessageServiceTest.java
    │       ├── MessageServiceTest.java
    │       └── UserServiceTest.java
    └── resources/
        └── application-test.properties
```

---

## ⚙️ Configuration

### `application.properties`
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5433/chatapp
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.jwt.secret=your_hex_secret_64chars
app.jwt.expiration=900000
app.jwt.refresh-expiration=604800000

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# File uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.upload.dir=uploads

# Server
server.port=8081
```

### Environment variables
Any property can be overridden with an environment variable (Spring's relaxed binding). Docker Compose and CI use these, so **no secrets need to be committed**:

| Variable | Property |
|----------|----------|
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | `spring.datasource.*` |
| `SPRING_DATA_REDIS_HOST` / `_PORT` | `spring.data.redis.*` (Spring Boot 3 — not `SPRING_REDIS_*`) |
| `APP_JWT_SECRET` | `app.jwt.secret` (64+ hex characters) |
| `APP_JWT_EXPIRATION` / `APP_JWT_REFRESH_EXPIRATION` | token lifetimes in ms |

---

## 🚀 Running Locally

### Prerequisites
- Java 21
- Maven 3.x
- Docker Desktop

### With Docker Compose (recommended)
1. Create your environment file and fill in real values:
   ```bash
   cp .env.example .env
   ```
   - `POSTGRES_PASSWORD` — database password
   - `APP_JWT_SECRET` — generate one with `openssl rand -hex 32`

   `.env` is git-ignored — never commit it.
2. Start everything:
   ```bash
   docker compose up --build
   ```
3. Open `http://localhost:8081`

**Changing host ports.** If a port is already in use, set `APP_PORT`, `DB_PORT` or `REDIS_PORT` in `.env`
(for example `APP_PORT=8082`, then open `http://localhost:8082`). Containers still talk to each other on the standard internal ports.

**Data.** Two named volumes keep your data across rebuilds: `postgres_data` (database) and `uploads_data` (uploaded files).
`docker compose down` keeps them; `docker compose down -v` **deletes them**.

> Note: `POSTGRES_PASSWORD` is only applied when the database volume is first created. To change it later, run
> `ALTER USER postgres PASSWORD '...'` inside the db container, then update `.env`.

### Without Docker (development)
1. Start PostgreSQL on port `5433`, database `chatapp`
2. Start Redis on port `6379`
3. Run the app:
```bash
mvn spring-boot:run
```

---

## 🧪 Testing

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test "-Dtest=JwtServiceTest"
```

### Test coverage
| Class | Type | Tests |
|-------|------|-------|
| `JwtServiceTest` | Unit | 6 |
| `ChatRoomServiceTest` | Unit | 9 |
| `MessageServiceTest` | Unit | 10 |
| `UserServiceTest` | Unit | 11 |
| `DirectMessageServiceTest` | Unit | 3 |
| `AuthControllerIntegrationTest` | Integration | 5 |
| `ChatAppApplicationTests` | Context load | 1 |

> `AuthControllerIntegrationTest` uses **Testcontainers** — Docker must be running.
> `ChatAppApplicationTests` boots the full app, so it needs PostgreSQL and Redis reachable
> (locally, or via the environment variables above — this is how CI runs it).

### Continuous integration
`.github/workflows/ci.yml` runs `mvn verify` on every push and pull request to `main`, with PostgreSQL and Redis
started as service containers.

---

## 🌐 API Overview

### Auth — `/api/account`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | ❌ | Register new user |
| POST | `/login` | ❌ | Login, receive tokens |
| POST | `/refresh` | ❌ | Refresh access token |
| POST | `/logout` | ❌ | Revoke refresh token |

### Rooms — `/api/rooms`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | ✅ | Get all rooms |
| POST | `/` | ✅ | Create room |
| PUT | `/{id}` | ✅ | Rename room (creator only) |
| DELETE | `/{id}` | ✅ | Delete room (creator only) |

### Messages — `/api/rooms/{roomId}`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/messages?before={id}&limit=30` | ✅ | Get a page of messages (oldest → newest) |
| POST | `/messages` | ✅ | Send message |
| PUT | `/messages/{id}` | ✅ | Edit message |
| DELETE | `/messages/{id}` | ✅ | Delete message |
| GET | `/messages/search?q=` | ✅ | Search messages |
| PUT | `/messages/{id}/pin` | ✅ | Toggle pin |
| GET | `/pinned` | ✅ | Get pinned message |
| POST | `/read` | ✅ | Mark room as read |

**Pagination.** Without `before`, the newest `limit` messages are returned. To load older history, pass the id of the
oldest message you already have as `before`. `limit` defaults to 30 and is capped at 100. A page shorter than `limit`
means there is nothing older.

### Direct Messages — `/api/dm`
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/{username}?before={id}&limit=30` | ✅ | Get a page of the conversation (same paging as rooms) |
| POST | `/{username}` | ✅ | Send DM |
| DELETE | `/{username}/{messageId}` | ✅ | Delete own DM |

### Rate limits
| Endpoints | Limit | Keyed by |
|-----------|-------|----------|
| `/api/account/login`, `/register` | 5 / min | IP |
| `/api/account/refresh` | 10 / min | IP |
| All other `/api/**` | 60 / min | username |

When exceeded, the API responds `429` with a `Retry-After` header and `{"error": "...", "retryAfterSeconds": N}`;
the web client shows a "try again in N s" message.

### Users
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/users` | ✅ | All users with online status |
| GET | `/api/online-users` | ✅ | Online usernames |
| GET | `/api/unread` | ✅ | Unread counts per room |
| GET | `/api/profile` | ✅ | Get own profile |
| PUT | `/api/profile` | ✅ | Update profile |
| PUT | `/api/profile/avatar` | ✅ | Update avatar URL |

### Files
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/files/upload` | ✅ | Upload file/image |

### Docs
| URL | Description |
|-----|-------------|
| `/swagger-ui.html` | Swagger UI |
| `/api-docs` | OpenAPI JSON |

---

## 📡 WebSocket Events

### Client → Server (`/app/...`)
| Destination | Payload | Description |
|-------------|---------|-------------|
| `/chat.send/{roomId}` | `{content, replyToId?, replyToSender?, replyToContent?}` | Send message |
| `/chat.edit/{roomId}/{messageId}` | `{content}` | Edit message |
| `/chat.delete/{roomId}/{messageId}` | — | Delete message |
| `/chat.react/{roomId}/{messageId}` | `{emoji}` | Toggle reaction |
| `/chat.pin/{roomId}/{messageId}` | — | Toggle pin |
| `/typing/{roomId}` | — | Typing indicator |
| `/dm.send/{username}` | `{content, replyToId?, ...}` | Send DM |

### Server → Client (`/topic/...`)
| Topic | Payload | Description |
|-------|---------|-------------|
| `/topic/room.{roomId}` | `MessageItemDTO` | New/edited message |
| `/topic/typing.{roomId}` | `username` | Typing indicator |
| `/topic/pin.{roomId}` | `MessageItemDTO` | Pin update |
| `/topic/dm.{username}` | `DMItemDTO` | DM received |
| `/topic/online-users` | `Set<String>` | Online users update |
| `/topic/unread.{username}` | `Map<roomId, count>` | Unread counts |

---

## 🐳 Docker Compose

```yaml
services:
  db:      # PostgreSQL 17 — health check, volume postgres_data
  redis:   # Redis 7 Alpine — health check
  app:     # Spring Boot app — waits for healthy db/redis, volume uploads_data
```

The app image is built in two stages (Maven build → slim JRE 21 runtime), runs as a non-root user and reads all
secrets from environment variables (see `.env.example`).

---

## 📋 Roadmap

### Completed ✅
- JWT auth + refresh tokens
- Real-time chat (rooms + DMs)
- Rate limiting (with user-facing feedback)
- Redis for online users
- Swagger UI
- Message search
- Unread message tracking
- File/image uploads
- @mentions with tags
- Avatars (generated + upload)
- Dark/light theme
- Mobile-responsive (iPhone tested)
- Reply to messages
- Pin messages
- Reactions + emoji picker
- Date separators
- WebSocket auto-reconnect
- Swipe navigation on mobile
- Unit + integration tests
- CI pipeline (GitHub Actions)
- Docker hardening: multi-stage build, non-root user, health checks, env-based secrets, uploads volume
- Lazy loading (cursor-based pagination on scroll) for rooms and DMs

### Planned 🔲
- Scroll to bottom button
- Typing indicator in DMs
- Show server validation messages on the login/register forms
- Docker image build check in CI
- Full-text search across all rooms
- Push notifications (PWA)
- Read receipts in DMs
- Message forwarding
