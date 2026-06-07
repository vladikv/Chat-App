# 💬 Chat App

A real-time chat application built with **Java Spring Boot** and **WebSocket**, featuring a clean iMessage-inspired UI.

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

---

## 🛠 Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 21 | Core language |
| Spring Boot 3.5 | Application framework |
| Spring Security | JWT authentication |
| Spring WebSocket + STOMP | Real-time messaging |
| Hibernate / JPA | ORM and database access |
| PostgreSQL 17 | Relational database |
| Maven | Build tool |
| Docker | Containerization |

### Frontend
| Technology | Purpose |
|---|---|
| HTML / CSS / JavaScript | UI |
| SockJS + STOMP.js | WebSocket client |

---

## 🚀 Getting Started

### Prerequisites
- Java 21
- PostgreSQL 17
- Maven

---

## 📁 Project Structure

```
src/main/java/com/example/chat_app/
├── config/
│   ├── JwtService.java          # JWT token generation & validation
│   ├── JwtAuthFilter.java       # Request authentication filter
│   ├── SecurityConfig.java      # Security rules & password encoder
│   ├── WebSocketConfig.java     # WebSocket & STOMP configuration
│   └── WebSocketEventListener.java  # Online/offline tracking
├── controllers/
│   ├── AuthController.java      # Register & login endpoints
│   ├── ChatController.java      # WebSocket message handler
│   ├── ChatRoomController.java  # Room CRUD endpoints
│   ├── MessageController.java   # Message endpoints
│   ├── OnlineUserController.java # Online users endpoint
│   └── UserController.java      # Profile endpoints
├── dto/
│   ├── account/                 # Auth & profile DTOs
│   ├── chatroom/                # Room DTOs
│   └── message/                 # Message DTOs
├── entities/
│   ├── UserEntity.java
│   ├── ChatRoomEntity.java
│   └── MessageEntity.java
├── repositories/
│   ├── UserRepository.java
│   ├── ChatRoomRepository.java
│   └── MessageRepository.java
└── services/
    ├── UserService.java
    ├── ChatRoomService.java
    ├── MessageService.java
    └── OnlineUserService.java
```

---

## 🔌 API Endpoints

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/account/register` | Register new user | ❌ |
| POST | `/api/account/login` | Login | ❌ |
| GET | `/api/profile` | Get user profile | ✅ |
| PUT | `/api/profile` | Update profile | ✅ |
| GET | `/api/rooms` | Get all rooms | ✅ |
| POST | `/api/rooms` | Create room | ✅ |
| PUT | `/api/rooms/{id}` | Rename room | ✅ |
| GET | `/api/rooms/{id}/messages` | Get messages | ✅ |
| POST | `/api/rooms/{id}/messages` | Send message | ✅ |
| PUT | `/api/rooms/{id}/messages/{msgId}` | Edit message | ✅ |
| DELETE | `/api/rooms/{id}/messages/{msgId}` | Delete message | ✅ |
| GET | `/api/users` | Get all users | ✅ |
| GET | `/api/online-users` | Get online users | ✅ |

---

## 🔄 WebSocket Topics

| Destination | Description |
|---|---|
| `/app/chat.send/{roomId}` | Send message |
| `/app/chat.edit/{roomId}/{messageId}` | Edit message |
| `/app/chat.delete/{roomId}/{messageId}` | Delete message |
| `/app/typing/{roomId}` | Typing indicator |
| `/topic/room.{roomId}` | Receive room messages |
| `/topic/typing.{roomId}` | Receive typing events |
| `/topic/online-users` | Receive online users updates |
