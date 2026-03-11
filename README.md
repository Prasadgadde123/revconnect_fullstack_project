# RevConnect

RevConnect is a modern, full-stack social media web application designed for seamless connection and engagement. Built with a premium, glassmorphic UI, it provides a feature-rich experience for personal users, creators, and businesses alike.

## 🚀 Key Features

### 👤 User Engagement
- **Dynamic Feed:** Interactive post feed with support for likes, comments, and sharing.
- **Rich Profiles:** Personalized profile cards for Personal, Business, and Creator accounts.
- **Real-time Notifications:** Stay updated with likes, new followers, and messages.
- **Direct Messaging:** Private communication channel for users.

### 🛡️ Admin & Control
- **Admin Dashboard:** Comprehensive oversight of platform activity with real-time stats and gauges.
- **Reporting System:** Robust user-reporting mechanism for platform safety.
- **User Management:** Search and manage users efficiently.

### 💼 Business Features
- **Product Listings:** Specialized tools for business accounts to showcase products.
- **Glassmorphic UI:** Cutting-edge design aesthetics for a premium brand feel.

## 📚 Technical Documentation

For in-depth technical details, please refer to the following documentation files:

- **[System Architecture](docs/ARCHITECTURE.md):** Detailed breakdown of the N-tier architecture, data flow, and security.
- **[ER Diagram (ERD)](docs/ERD.md):** Visual representation of the database schema and entity relationships.

## 📂 Project Structure

```text
revconnect_fullstack_project/
├── docs/                      # Technical documentation
│   ├── ARCHITECTURE.md        # System architecture details
│   └── ERD.md                 # Entity Relationship Diagram
├── src/
│   ├── main/
│   │   ├── java/com/revconnect/
│   │   │   ├── config/        # Security and application configuration
│   │   │   ├── controller/    # Web controllers (API endpoints)
│   │   │   ├── dto/           # Data Transfer Objects (DTOs)
│   │   │   ├── entity/        # JPA Database Entities
│   │   │   ├── enums/         # User roles and post types
│   │   │   ├── exception/     # Custom error handling
│   │   │   ├── repository/    # Data access layer (Interfaces)
│   │   │   ├── service/       # Business logic implementation
│   │   │   └── RevConnectAppApplication.java
│   │   └── resources/
│   │       ├── static/        # Static assets (CSS, JS, images)
│   │       └── templates/     # Thymeleaf HTML templates (UI)
│   └── test/                  # Unit and integration test suites
├── pom.xml                    # Maven dependencies and build config
└── README.md                  # Project overview and guide
```

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.2
- **Security:** Spring Security (Custom Auth with Username/Email support)
- **Database:** H2 (Dev) / MySQL (Prod)
- **Frontend:** Thymeleaf, Vanilla CSS, JavaScript

## 🏁 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd revconnect_fullstack_project
   ```
2. Build the project:
   ```bash
   mvn clean install
   ```

### Running Locally
Run the application using the Maven wrapper:
```bash
./mvnw spring-boot:run
```
The application will be accessible at `http://localhost:8080`.

## 🎨 Design Philosophy
RevConnect focuses on "Glassmorphism" — utilizing subtle transparency, vibrant accents, and smooth animations to create a state-of-the-art visual experience.

---
Built with ❤️ by the RevConnect Team.
