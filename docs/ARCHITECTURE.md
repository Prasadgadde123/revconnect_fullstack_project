# System Architecture — RevConnect

RevConnect is engineered as a robust, scalable social media platform leveraging the Spring Boot ecosystem. This document outlines the architectural patterns, data flow, and security framework within the system.

---

## 🏗️ Architectural Overview

The application follows the **N-tier Architecture** pattern, ensuring a clean separation of concerns and high maintainability.

```
┌─────────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                          │
│           Thymeleaf · Vanilla CSS · Vanilla JS                  │
│              templates/  ·  static/  ·  fragments/              │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP Request / Response
┌──────────────────────────▼──────────────────────────────────────┐
│                      CONTROLLER LAYER                           │
│     Spring MVC · @Controller · @RestController · DTOs           │
│  AuthController · PostController · ProfileController · +12 more │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Service Call
┌──────────────────────────▼──────────────────────────────────────┐
│                       SERVICE LAYER                             │
│          @Service · Business Logic · RBAC · Scheduler           │
│  UserService · PostService · ConnectionService · +5 more        │
└──────────────────────────┬──────────────────────────────────────┘
                           │ Repository Call
┌──────────────────────────▼──────────────────────────────────────┐
│                    DATA ACCESS LAYER                            │
│          Spring Data JPA · Hibernate · Entity Mapping           │
│   UserRepository · PostRepository · CommentRepository · +5 more │
└──────────────────────────┬──────────────────────────────────────┘
                           │ SQL / JPA
              ┌────────────┴────────────┐
              ▼                         ▼
       ┌─────────────┐          ┌──────────────┐
       │  H2 Database│          │MySQL Database│
       │    (Dev)    │          │    (Prod)    │
       └─────────────┘          └──────────────┘
```

---

## 📐 Layer Details

### 1. Presentation Layer (Frontend)
- **Technology:** Thymeleaf, Vanilla CSS, Vanilla JS
- **Responsibility:** Server-side rendering of views, handling client-side interactions, and presenting a modern **Glassmorphic UI**
- **Key Files:** `src/main/resources/templates/`, `src/main/resources/static/`

### 2. Controller Layer (Web/API)
- **Technology:** Spring MVC (`@Controller`, `@RestController`)
- **Responsibility:** Handling HTTP requests, validating user input via DTOs, and orchestrating the flow between the view and the service layer
- **Components:** `com.revconnect.controller`

| Controller | Responsibility |
|---|---|
| `AuthController` | Login, register, forgot password |
| `PostController` | CRUD, likes, comments, share |
| `ProfileController` | View, edit, analytics |
| `ConnectionController` | Send, accept, reject requests |
| `FeedController` | Personalized and explore feed |
| `NotificationController` | View, mark as read |
| `AdminController` | Dashboard, user management, reports |
| `ProductController` | Business product listings |
| `MessageController` | Direct messaging |

### 3. Service Layer (Business Logic)
- **Technology:** Spring `@Service`
- **Responsibility:** Implementing core business rules — authentication, social networking logic, post filtering, scheduling, and notifications. Completely decoupled from the web and data access layers
- **Components:** `com.revconnect.service`

| Service | Responsibility |
|---|---|
| `UserService` | Registration, auth, profile, follow/unfollow |
| `PostService` | Post CRUD, likes, comments, hashtags, feed |
| `ConnectionService` | Send/accept/reject connection requests |
| `NotificationService` | Create and deliver in-app notifications |
| `AdminService` | Reports, platform stats, announcements |
| `AnalyticsService` | Engagement metrics, follower demographics |
| `ProductService` | Business product management |
| `PostSchedulerService` | Auto-publish scheduled posts every 60s |

### 4. Data Access Layer (Persistence)
- **Technology:** Spring Data JPA, Hibernate
- **Responsibility:** Managing database interactions, entity mapping, and repository abstractions
- **Components:** `com.revconnect.repository`, `com.revconnect.entity`

---

## 🔄 Data Flow

```
  User (Browser)
       │
       │  1. Interact (e.g. submit a post)
       ▼
  DispatcherServlet
       │
       │  2. Route to correct Controller
       ▼
  Controller  ──── DTO Validation
       │
       │  3. Pass validated data to Service
       ▼
  Service Layer ──── Business Rules / RBAC
       │
       │  4. Call Repository
       ▼
  Repository (JPA)
       │
       │  5. Read / Write
       ▼
  Database (H2 / MySQL)
       │
       │  6. Return data up the chain
       ▼
  Controller ──── ModelAndView
       │
       │  7. Thymeleaf renders HTML
       ▼
  User (Browser) ◄── Response
```

### Log4j2 observes every step

```
INFO  — method started / completed successfully
DEBUG — internal values, counts, query details
WARN  — unauthorized attempts, suppressed notifications
ERROR — exceptions, resource not found, publish failures
```

Log output locations:
- `logs/revconnect.log` — all levels (INFO, DEBUG, WARN, ERROR)
- `logs/revconnect-error.log` — ERROR level only

---

## 🔐 Security Framework

RevConnect utilizes **Spring Security** for a comprehensive security posture.

```
HTTP Request
     │
     ▼
Spring Security Filter Chain
     │
     ├── Authentication ──► Custom UserDetailsService
     │                       (supports Username OR Email login)
     │
     ├── Authorization ───► Role-Based Access Control (RBAC)
     │                       PERSONAL · CREATOR · BUSINESS · ADMIN
     │
     └── Protection ──────► CSRF · XSS · SQL Injection guards
```

### Role Permissions

| Feature | PERSONAL | CREATOR | BUSINESS | ADMIN |
|---|:---:|:---:|:---:|:---:|
| Create posts | ✅ | ✅ | ✅ | ✅ |
| Scheduled posts | ❌ | ✅ | ✅ | ✅ |
| CTA buttons on posts | ❌ | ✅ | ✅ | ✅ |
| Product listings | ❌ | ❌ | ✅ | ✅ |
| Post analytics | ❌ | ✅ | ✅ | ✅ |
| Admin dashboard | ❌ | ❌ | ❌ | ✅ |
| Manage reports | ❌ | ❌ | ❌ | ✅ |
| Broadcast announcements | ❌ | ❌ | ❌ | ✅ |

---

## 🚀 Design Principles

- **Glassmorphism** — UI aesthetic using background blur, transparency, and vibrant accents for a premium feel
- **SOLID** — Adherence to object-oriented design principles ensuring code modularity and maintainability
- **Responsive Design** — Optimized for seamless experience across desktop and mobile devices
- **Separation of Concerns** — Each layer has a single, well-defined responsibility
- **DRY (Don't Repeat Yourself)** — Shared logic centralized in services and fragments

---

## 📁 Package Structure

```
com.revconnect/
├── config/          # SecurityConfig, WebConfig, DataInitializer
├── controller/      # 17 controllers (MVC + REST)
├── dto/             # Data Transfer Objects for input validation
├── entity/          # JPA entities (User, Post, Comment, etc.)
├── enums/           # UserRole, PostType, NotificationType, etc.
├── exception/       # GlobalExceptionHandler, ResourceNotFoundException
├── repository/      # 9 Spring Data JPA repository interfaces
└── service/         # 8 business logic services
```

---

*Technical Architecture Documentation for RevConnect.*
