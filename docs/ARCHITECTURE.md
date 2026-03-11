# System Architecture - RevConnect

RevConnect is engineered as a robust, scalable social media platform leveraging the Spring Boot ecosystem. This document outlines the architectural patterns and data flow within the system.

## 🏗️ Architectural Overview

The application follows the **N-tier Architecture** pattern, ensuring a clean separation of concerns and high maintainability.

### 1. Presentation Layer (Frontend)
- **Technology:** Thymeleaf, Vanilla CSS, Vanilla JS.
- **Responsibility:** Server-side rendering of views, handling client-side interactions, and presenting a modern **Glassmorphic UI**.
- **Key Files:** `src/main/resources/templates/`, `src/main/resources/static/`.

### 2. Controller Layer (Web/API)
- **Technology:** Spring MVC (@Controller, @RestController).
- **Responsibility:** Handling HTTP requests, validating user input via DTOs, and orchestrating the flow between the view and the service layer.
- **Components:** `com.revconnect.controller`.

### 3. Service Layer (Business Logic)
- **Technology:** Spring @Service.
- **Responsibility:** Implementing core business rules (e.g., authentication, social networking logic, post filtering). This layer is completely decoupled from the web and data access layers.
- **Components:** `com.revconnect.service`.

### 4. Data Access Layer (Persistence)
- **Technology:** Spring Data JPA, Hibernate.
- **Responsibility:** Managing database interactions, entity mapping, and repository abstractions.
- **Components:** `com.revconnect.repository`, `com.revconnect.entity`.

## 🔐 Security Framework

RevConnect utilizes **Spring Security** for a comprehensive security posture:
- **Authentication:** Custom implementation supporting both Username and Email identifiers.
- **Authorization:** Role-based access control (RBAC) supporting `PERSONAL`, `CREATOR`, `BUSINESS`, and `ADMIN` roles.
- **Protection:** Built-in guards against CSRF, XSS, and SQL Injection.

## 🔄 Data Flow

1. **Request:** User interacts with the UI (e.g., submits a post).
2. **Routing:** `DispatcherServlet` routes the request to the appropriate `Controller`.
3. **Validation:** `Controller` validates data and passes it to the `Service` layer.
4. **Logic:** `Service` layer processes business rules and calls the `Repository`.
5. **Persistence:** `Repository` interacts with the database (H2/MySQL).
6. **Response:** Data flows back up to the `Controller`, which returns a `ModelAndView` rendered by `Thymeleaf`.

## 🚀 Design Principles
- **Glassmorphism:** A design aesthetic characterized by background blur and transparency.
- **SOLID:** Adherence to object-oriented design principles to ensure code modularity.
- **Responsive Design:** Optimized for a seamless experience across desktop and mobile devices.

---
*Technical Architecture Documentation for RevConnect.*
