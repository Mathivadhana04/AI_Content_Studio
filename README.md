# AI Content Studio

An enterprise AI-powered content generation SaaS platform built for high-growth creator teams. Generate SEO-optimized blog posts, run inline tone adjustments, manage editorial calendar schedules, and distribute social updates.

---

## 🚀 Key Modules & Features

- **Authentication & RBAC**: Fully-featured JWT authentication with rotation and role enforcement.
- **AI Content Generation**: Powered by **Groq** (using Llama 3.3 models) and **Ollama** (offline fallback).
- **SEO Quality Check**: Rule-based analysis score metrics tracking keyword density, length, and headings.
- **Editorial Scheduler**: Cron-based auto-publishing scheduler engine.
- **Threaded Collaborations**: Mentions, resolves, and comment threads.
- **Social & Email Copy**: Distribution outputs tailored for LinkedIn, Twitter, and HTML newsletters.

---

## 🛠 Tech Stack

- **Backend**: Spring Boot 3.3, Spring Security 6, Spring Data JPA, Hibernate, WebSocket (STOMP), MySQL.
- **Frontend**: Vite + React 18, React Router v6, Material UI (MUI v5), Tailwind CSS, Recharts, React Quill.

---

## 📦 Setting Up Locally

### 1. Database Configuration
You can start a local MySQL instance with Docker:
```bash
docker-compose up -d
```
Alternatively, configure a local instance and verify that a schema named `ai_content_studio_db` exists.

### 2. Launching Backend
1. Go to the `backend` folder.
2. Setup values inside `src/main/resources/application.yml` or copy variables from `.env.example`.
3. Build and launch:
```bash
mvn clean install
mvn spring-boot:run
```

### 3. Launching Frontend
1. Go to the `frontend` folder.
2. Initialize npm dependencies:
```bash
npm install
```
3. Run development server:
```bash
npm run dev
```
Open `http://localhost:5173` to view the platform.

---

## 📄 OpenAPI / Swagger Documentation
Once the backend is running, you can explore the REST endpoints via Swagger UI:
`http://localhost:8080/swagger-ui.html`
