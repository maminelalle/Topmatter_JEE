# Topmatter_JEE (Topmatter)

A mini social network application built with **Spring Boot** (backend) and **Angular** (frontend).

## Tech stack

### Backend
- Java 17
- Spring Boot (Web, Data JPA, Security, WebSocket)
- PostgreSQL
- JWT authentication

### Frontend
- Angular 16
- Tailwind CSS

## Project structure

- `backend/` : Spring Boot API
- `frontend/` : Angular application
- `screenshot/` : application screenshots

## Screenshots

### Auth (Login / Register)
![Auth](screenshot/auth.png)

### Friends
![Friends](screenshot/friends.png)

### Home / Feed
![Home](screenshot/home.png)

## Run with Docker (recommended)

From the repository root:

```bash
docker-compose up -d --build
```

- Frontend: http://localhost:80
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5432

Default database credentials (as defined in `docker-compose.yml`):
- Database: `social_network`
- User: `postgres`
- Password: `postgres`

## Run locally (without Docker)

### 1) Start PostgreSQL

Make sure you have a running PostgreSQL instance and configure the backend datasource accordingly.

### 2) Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

The API should start on: http://localhost:8080

### 3) Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

The Angular app should start on: http://localhost:4200

## Notes

- This repository contains helper scripts (`*.ps1`) that can be used on Windows to speed up setup.
- Feel free to open an issue or submit improvements.