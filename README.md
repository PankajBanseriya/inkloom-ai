# Inkloom AI — Intelligent Brand Governance Platform

> An enterprise full-stack platform ensuring marketing and communications comply with brand guidelines, approved claims, terminology, and tone of voice.

---

## 📌 Architecture Overview

Inkloom AI is structured as a clean monorepo:

* **`frontend/`**: React 18 single page application powered by Vite, Tailwind CSS, Lucide React, and React Router. Implemented with clean, maintainable modern JavaScript (ES6+ / JSX).
* **`backend/`**: Java 21+ Spring Boot REST API following standard Spring MVC, Spring Security, and Spring Data MongoDB architectures.
* **`docs/`**: Comprehensive system architecture, REST API contracts, and milestone development plans.

---

## 🚀 Quickstart

### Prerequisites
* **Node.js**: v20+ (tested with Node v24 LTS) & `npm`
* **Java**: OpenJDK 21+ (tested with OpenJDK 25 LTS)
* **MongoDB**: Local MongoDB instance (`mongodb://localhost:27017/inkloom`) or a MongoDB Atlas connection string.

---

### 1. Backend Setup

```bash
cd backend

# On Windows:
.\mvnw.cmd clean compile
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run

# On Linux/macOS:
./mvnw clean compile
./mvnw test
./mvnw spring-boot:run
```

The Spring Boot backend starts at `http://localhost:8080`.
Verify status:
```bash
curl http://localhost:8080/api/v1/health
```

### 2. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev

# Run production build
npm run build
```

The frontend will run at `http://localhost:5173`.

---

## 🔒 Security & AI Boundaries
* **AI Provider Independence**: External AI (e.g. Gemini, OpenAI) is orchestrated exclusively by the backend service layer.
* **No Client Keys**: API credentials and database connection strings are never exposed to the browser or bundled into the frontend.
* **Deterministic Fallback**: In environments where no external AI provider key is configured, the system performs local deterministic rule checks and reports AI status accurately.
* **Tenant Isolation**: Brand workspaces are strictly partitioned via backend authorization checks.

---

## 📄 License
Inkloom AI Platform — Proprietary & Confidential.
