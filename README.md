# Movies API Challenge

REST API to retrieve directors with movie count above a given threshold. This API fetches movies from an external source and filters directors based on the number of movies they have directed.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Performance Optimizations](#performance-optimizations)
- [Code Quality](#code-quality)
- [Configuration](#configuration)

---

## 🎯 Overview

This application consumes an external Movies API and provides an endpoint to retrieve directors who have directed more than a specified threshold number of movies. The results are returned in alphabetical order.

**Key Features:**
- ✅ Hexagonal Architecture (Ports & Adapters)
- ✅ Parallel HTTP requests for improved performance
- ✅ OpenAPI/Swagger documentation
- ✅ Global exception handling
- ✅ Code quality tools (Spotless, PMD, SpotBugs, Checkstyle)
- ✅ MapStruct for DTO mapping
- ✅ Spring Cloud OpenFeign for HTTP client

---

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports and Adapters pattern):

```
┌─────────────────────────────────────────────────────────────┐
│                     Infrastructure Layer                     │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ Controller   │    │   Adapter    │    │ Feign Client │  │
│  │   (API)      │    │ (MoviesPort) │    │  (External)  │  │
│  └──────┬───────┘    └──────┬───────┘    └──────────────┘  │
└─────────┼──────────────────┼──────────────────────────────┘
          │                  │
┌─────────▼──────────────────▼──────────────────────────────┐
│                    Application Layer                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          DirectorService (Use Case)                  │  │
│  │          AsyncMoviesService (Parallel Calls)         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────┬──────────────────────────────────┘
                          │
┌─────────────────────────▼──────────────────────────────────┐
│                      Domain Layer                           │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │    Model     │    │    Port      │    │  Exception   │  │
│  │   (Movie)    │    │(MoviesPort)  │    │   (Custom)   │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

**Layer Responsibilities:**

- **Domain**: Core business logic, entities, and port interfaces
- **Application**: Use cases and business rules orchestration
- **Infrastructure**: External concerns (API, database, HTTP clients)

---

## 🛠️ Technologies

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Cloud 2025.0.0** (Northfields)
- **Spring Cloud OpenFeign** - HTTP client
- **MapStruct 1.5.5** - DTO mapping
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI 2.8.4** - API documentation
- **Maven** - Build tool

**Code Quality Tools:**
- Spotless (Google Java Format)
- PMD (Static analysis)
- SpotBugs (Bug detection)
- Checkstyle (Code style)

---

## 📁 Project Structure

```
src/main/java/com/challenge/movies/
├── domain/
│   ├── model/
│   │   ├── Movie.java
│   │   └── MoviesResponse.java
│   ├── port/
│   │   └── MoviesPort.java
│   └── exception/
│       └── InvalidParameterException.java
│
├── application/
│   └── service/
│       ├── DirectorService.java
│       └── AsyncMoviesService.java
│
└── infrastructure/
    ├── adapter/
    │   └── MoviesAdapter.java
    ├── client/
    │   ├── MoviesClient.java
    │   ├── dto/
    │   │   ├── MovieDto.java
    │   │   └── MoviesResponseDto.java
    │   └── mapper/
    │       └── MoviesMapper.java
    ├── controller/
    │   ├── DirectorController.java
    │   ├── dto/
    │   │   ├── DirectorsResponseDto.java
    │   │   └── ErrorResponseDto.java
    │   ├── mapper/
    │   │   └── DirectorMapper.java
    │   └── exception/
    │       └── GlobalExceptionHandler.java
    └── config/
        ├── AsyncConfig.java
        └── OpenApiConfig.java
```

---

## 🚀 Getting Started

### Prerequisites

- Java 21
- Maven 3.8+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd movies-api
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR:
   ```bash
   java -jar target/movies-0.0.1-SNAPSHOT.jar
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI Spec: http://localhost:8080/api-docs

---

## 📚 API Documentation

### Endpoint

**GET** `/api/directors`

Retrieves a list of director names who have directed more than the specified threshold number of movies.

**Parameters:**
| Name      | Type    | Required | Description                              | Example |
|-----------|---------|----------|------------------------------------------|---------|
| threshold | Integer | Yes      | Minimum number of movies (must be ≥ 0)  | 4       |

**Responses:**

**200 OK**
```json
{
  "directors": [
    "Martin Scorsese",
    "Woody Allen"
  ]
}
```

**400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Threshold must be a non-negative integer",
  "path": "/api/directors",
  "timestamp": "2025-10-30T12:34:56"
}
```

**500 Internal Server Error**
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please try again later.",
  "path": "/api/directors",
  "timestamp": "2025-10-30T12:34:56"
}
```

### Examples

```bash
# Get directors with more than 4 movies
curl http://localhost:8080/api/directors?threshold=4

# Invalid threshold (negative)
curl http://localhost:8080/api/directors?threshold=-1

# Invalid threshold (non-numeric)
curl http://localhost:8080/api/directors?threshold=abc
```

---

## ⚡ Performance Optimizations

### Parallel HTTP Requests

The application uses **CompletableFuture** with **@Async** to fetch multiple pages from the external API in parallel.

**Strategy:**
1. First HTTP call retrieves `total_pages`
2. Remaining pages (2-N) are fetched concurrently
3. All results are combined and processed

**Configuration:**

```yaml
movies:
  async:
    core-pool-size: 5      # Minimum threads
    max-pool-size: 10      # Maximum threads
    queue-capacity: 50     # Task queue capacity
```

**Performance Gains:**

| Scenario        | Sequential Time | Parallel Time | Improvement |
|-----------------|-----------------|---------------|-------------|
| 10 pages @ 1s   | ~10 seconds     | ~2 seconds    | **80%**     |
| 20 pages @ 1s   | ~20 seconds     | ~3 seconds    | **85%**     |

### Thread Pool Best Practices

- **I/O-bound operations**: `cores × 2` to `cores × 4`
- **Default**: 5-10 threads for external API calls
- **Rate limiting**: Adjust `max-pool-size` based on API limits
- **Graceful shutdown**: Configured with 60s timeout

---

## 🔍 Code Quality

### Running Quality Checks

```bash
# Format code with Spotless
mvn spotless:apply

# Check code formatting
mvn spotless:check

# Run all quality checks
mvn verify
```

### Quality Tools

**Spotless** - Code formatting
- Google Java Format style
- Automatic import organization

**PMD** - Static code analysis
- Best practices enforcement
- Code smell detection

**SpotBugs** - Bug detection
- Security vulnerabilities
- Performance issues

**Checkstyle** - Code style
- Google Java Style Guide

---

## ⚙️ Configuration

### application.yml

```yaml
movies:
  api:
    url: https://wiremock.dev.eroninternational.com
  async:
    core-pool-size: 5
    max-pool-size: 10
    queue-capacity: 50
    thread-name-prefix: MoviesAsync-

spring:
  application:
    name: movies
  cloud:
    compatibility-verifier:
      enabled: false

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
```

### Customization

**Increase parallelism:**
```yaml
movies:
  async:
    max-pool-size: 20  # More concurrent requests
```

**Rate limiting compliance:**
```yaml
movies:
  async:
    max-pool-size: 5   # Fewer concurrent requests
```

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

---

## 📦 Build & Deploy

```bash
# Build JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Build Docker image (if configured)
mvn spring-boot:build-image
```
