# RevConnect 🚀

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)

RevConnect is a modern, dynamic full-stack social media and professional networking web application built with Spring Boot and Thymeleaf. It bridges the gap between traditional networking platforms and casual social media by providing powerful tools for creators, businesses, and everyday users within a sleek, responsive, glassmorphic UI.

## ✨ Features

- **Rich, Premium UI**: Modern aesthetic featuring dark modes, vibrant gradients, glassmorphism templates, and smooth scrolling single-page landings. 
- **Roles & Permissions**: Support for multiple account types including `PERSONAL`, `CREATOR`, `BUSINESS`, and `ADMIN`.
- **Dynamic Feeds**: Trending posts, hashtag explorations, and a personalized feed.
- **Connections & Followers**: Send connection requests or simply follow your favorite creators.
- **Engagement**: Like, comment, share, and pin posts effortlessly.
- **Notifications**: Real-time alert system for connection requests and post interactions.
- **Admin Dashboard**: Built-in user and content management directly from the UI.
- **Security**: Robust authentication powered by Spring Security 6 with encrypted passwords.

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.0 (Web, Data JPA, Security, Validation)
- **Frontend**: HTML5, Vanilla CSS (Custom Design System), Thymeleaf
- **Database**: H2 (Development & Demo) / MySQL (Production Ready)
- **Build Tool**: Maven

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.6+

### Local Setup & Execution

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/revconnect.git
   cd revconnect
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   Open your browser and navigate to: `http://localhost:8080/`

### 🧑‍💻 Demo Accounts (Auto-Seeded)

The application utilizes an H2 in-memory database by default. On startup, `DataInitializer.java` automatically seeds several demo accounts for immediate testing:

| Role       | Username      | Password   | Description                   |
|------------|---------------|------------|-------------------------------|
| **Admin**  | `admin`       | `admin123` | Access to the Admin Dashboard |
| **Personal** | `alice`     | `password` | Standard user account         |
| **Personal** | `bob`       | `password` | Standard user account         |
| **Creator**| `techcreator` | `password` | Creator account               |
| **Business**| `shopnow`   | `password` | Business account              |

---

## 🛡️ Admin Dashboard
To access the admin panel, log in with the `admin` credentials listed above, and click the **"🛡️ Admin Panel"** link located in your left-hand side-nav or the top right profile dropdown menu.

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
  
---
*Built with ❤️ for the future of professional networking.*