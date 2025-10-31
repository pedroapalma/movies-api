# Movies API Challenge

REST API to retrieve directors with movie count above a given threshold. This API fetches movies from an external source and filters directors based on the number of movies they have directed.

## 🚀 Quick Links

| Resource | Link | Description |
|----------|------|-------------|
| 📮 **Postman Collection** | [`Download`](postman/Movies%20API.postman_collection.json) | Pre-configured OAuth2 authentication (zero setup!) |
| 📖 **API Documentation** | http://localhost:8080/swagger-ui.html | Interactive Swagger UI |
| 🏥 **Health Check** | http://localhost:8080/actuator/health | API health status |
| 🐳 **Docker Setup** | `src/main/resources/docker/` | Redis + Keycloak auto-import |
| 🔑 **Keycloak Admin** | http://localhost:8180 | admin / admin |

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Performance Optimizations](#performance-optimizations)
- [Caching Strategy](#-caching-strategy)
- [Rate Limiting](#-rate-limiting)
- [Authentication (OAuth2)](#-authentication-oauth2)
- [Postman Collection](#-postman-collection)
- [Code Quality](#-code-quality)
- [Configuration](#configuration)
- [Docker Setup](#-docker-setup)
- [Testing](#testing)
- [Build & Deploy](#build--deploy)

---

## 🎯 Overview

This application consumes an external Movies API and provides an endpoint to retrieve directors who have directed more than a specified threshold number of movies. The results are returned in alphabetical order.

**Key Features:**
- ✅ Hexagonal Architecture (Ports & Adapters)
- ✅ Parallel HTTP requests for improved performance (80%+ faster)
- ✅ Multi-profile caching with configurable TTL (Caffeine for local, Redis for production)
- ✅ Rate limiting with Bucket4j (configurable requests per minute)
- ✅ OAuth2 Client Credentials authentication (disabled in local, enabled in prod)
- ✅ **Ready-to-use Postman Collection** with pre-configured OAuth2 ([see below](#-postman-collection))
- ✅ Docker Compose setup for Redis, Keycloak with auto-import realm
- ✅ Detailed performance logging and cache monitoring
- ✅ OpenAPI/Swagger documentation
- ✅ Global exception handling with custom domain exceptions
- ✅ Code quality tools (Spotless, PMD, SpotBugs, Checkstyle)
- ✅ MapStruct for DTO mapping
- ✅ Spring Cloud OpenFeign for HTTP client
- ✅ Spring Boot Actuator for health checks

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

- **Domain**: Core business logic, entities, port interfaces, and custom exceptions
- **Application**: Use cases and business rules orchestration with async processing
- **Infrastructure**: External concerns (API, database, HTTP clients, cache adapters)

**Key Design Decisions:**

1. **CachePort Interface** - Instead of `@Cacheable`, we use a port to maintain framework independence and hexagonal architecture purity
2. **Profile-Based Cache** - Caffeine for local development (fast, no infrastructure), Redis for production (distributed, scalable)
3. **Configurable via Properties** - Cache TTL, key prefixes, and pool sizes are externalized to `application.yml`
4. **Custom Exceptions** - Domain exceptions (`InvalidParameterException`, `MoviesFetchException`) for better error handling
5. **Async with Thread Pool** - Configurable thread pool for parallel API calls with proper resource management
6. **Detailed Logging** - INFO-level logs for cache performance tracking without debug overhead

---

## 🛠️ Technologies

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Cloud 2025.0.0** (Northfields)
- **Spring Cloud OpenFeign** - HTTP client
- **Spring Security OAuth2 Resource Server** - JWT authentication
- **MapStruct 1.5.5** - DTO mapping
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI 2.8.4** - API documentation
- **Bucket4j 8.10.1** - Rate limiting with token bucket algorithm
- **Caffeine** - In-memory cache (local profile)
- **Redis** - Distributed cache (production profile)
- **Keycloak 23.0** - Authorization server (auto-configured)
- **Spring Boot Actuator** - Health checks and monitoring
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
│   │   ├── MoviesPort.java
│   │   ├── CachePort.java
│   │   └── SecurityPort.java
│   └── exception/
│       ├── InvalidParameterException.java
│       └── MoviesFetchException.java
│
├── application/
│   └── service/
│       ├── DirectorService.java
│       └── AsyncMoviesService.java
│
└── infrastructure/
    ├── adapter/
    │   ├── MoviesAdapter.java
    │   ├── cache/
    │   │   ├── CaffeineCacheAdapter.java (@Profile("local"))
    │   │   └── RedisCacheAdapter.java (@Profile("!local"))
    │   └── security/
    │       └── SecurityContextAdapter.java
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
    ├── interceptor/
    │   └── RateLimitInterceptor.java
    └── config/
        ├── AsyncConfig.java
        ├── OpenApiConfig.java
        ├── RedisConfig.java (@Profile("!local"))
        ├── SecurityConfig.java (@Profile-based)
        └── WebConfig.java

src/main/resources/
├── application.yml (Production - Redis + OAuth2)
├── application-local.yml (Development - Caffeine + No Auth)
└── docker/
    ├── docker-compose.yml (Redis + Keycloak + Management UIs)
    └── README.md (Docker setup instructions)
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

## 💾 Caching Strategy

The application implements a **multi-profile caching strategy** following hexagonal architecture principles.

### Architecture

```
DirectorService / AsyncMoviesService
         ↓ uses
    CachePort (interface - domain layer)
         ↑ implements
CaffeineCacheAdapter | RedisCacheAdapter
   (@Profile("local"))   (@Profile("!local"))
```

**Why not @Cacheable?**
Instead of using Spring's `@Cacheable` annotation, we implement caching through the `CachePort` interface to maintain **hexagonal architecture principles**:
- ✅ **Framework independence** - Domain layer doesn't depend on Spring
- ✅ **Better testability** - Easy to mock and test cache behavior
- ✅ **Architecture purity** - Cache is treated as an infrastructure concern
- ✅ **Flexibility** - Easy to swap implementations or add custom logic

### Cache Implementations

**Local Profile (Development)**
- **Technology**: Caffeine (in-memory cache)
- **Location**: `src/main/java/com/challenge/movies/infrastructure/adapter/cache/CaffeineCacheAdapter.java`
- **Advantages**: No infrastructure required, fast development
- **Configuration**: `application-local.yml`
- **Performance**: ~5-15ms per page

**Production Profile (Default)**
- **Technology**: Redis (distributed cache)
- **Location**: `src/main/java/com/challenge/movies/infrastructure/adapter/cache/RedisCacheAdapter.java`
- **Advantages**: Shared across instances, persistence, scalability
- **Configuration**: `application.yml`
- **Serialization**: Jackson with type information for polymorphic support

### Cache Behavior

**What is cached:**
- Individual API page responses (`MoviesResponse`)
- Cache key pattern: `movies:page:{pageNumber}`

**Cache configuration:**
```yaml
cache:
  ttl-minutes: 1              # Time to live (1 minute for demo purposes)
  max-size: 1000              # Max entries (Caffeine only)
  key-prefix: "movies:page:"  # Configurable cache key prefix
```

**How it works:**
1. **First request**: Page 1 is checked in cache, if MISS → fetch from API and store in cache
2. **Parallel requests**: Pages 2-N are fetched in parallel, each checks cache independently
3. **Optimization**: Each page is checked in cache only ONCE (no duplicate lookups)
4. **Logging**: INFO level shows individual cache HIT/MISS for each page with timing
5. **TTL**: Cache entries expire after 1 minute (configurable)
6. **Key prefix**: Configurable via `@Value` injection from application.yml

### Running with Different Profiles

**Local (Caffeine):**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Production (Redis):**
```bash
# Start Redis with Docker Compose (recommended)
cd src/main/resources/docker
docker-compose up -d

# Or using docker run
docker run -d -p 6379:6379 redis:latest

# Run application
mvn spring-boot:run
```

**View Redis cache (optional):**
- Redis Commander UI: http://localhost:8081
- See `src/main/resources/docker/README.md` for detailed Docker setup

### Performance Impact

**First Request (Cold Cache):**
```
========== Starting Movies Fetch ==========
Page 1: Fetched from API - 933 ms
Total pages to fetch: 3
Page 2: Fetched from API - 424 ms
Page 3: Fetched from API - 424 ms
Parallel fetch of 2 pages completed in 427 ms (avg: 213 ms/page)
========== Fetch Complete ==========
Total movies fetched: 27
Total pages: 3
Total time: 1478 ms (avg: 492 ms/page)
=======================================
```

**Subsequent Requests (Warm Cache - Redis):**
```
========== Starting Movies Fetch ==========
Page 1: Cache HIT - 2 ms
Total pages to fetch: 3
Page 2: Cache HIT - 1 ms
Page 3: Cache HIT - 1 ms
Parallel fetch of 2 pages completed in 2 ms (avg: 1 ms/page)
========== Fetch Complete ==========
Total movies fetched: 27
Total pages: 3
Total time: 5 ms (avg: 1 ms/page)
=======================================
```

**Subsequent Requests (Warm Cache - Caffeine/Local):**
```
========== Starting Movies Fetch ==========
Page 1: Cache HIT - 11 ms
Total pages to fetch: 3
Page 2: Cache HIT - 1 ms
Page 3: Cache HIT - 1 ms
Parallel fetch of 2 pages completed in 1 ms (avg: 0 ms/page)
========== Fetch Complete ==========
Total movies fetched: 27
Total pages: 3
Total time: 13 ms (avg: 4 ms/page)
=======================================
```

**Performance Improvement:**
- **With Redis**: 99.7% faster (1478ms → 5ms)
- **With Caffeine**: 99.1% faster (1478ms → 13ms)

**Note:** First request after app restart with Redis takes ~400-500ms due to connection pool initialization. Subsequent requests are ~5-10ms.

### Cache Monitoring

**Default logging (INFO level):**
The application logs individual page cache status at INFO level by default:
```yaml
logging:
  level:
    com.challenge.movies: INFO
```

**Output shows:**
```
INFO DirectorService    : Page 1: Cache HIT - 2 ms
INFO AsyncMoviesService : Page 2: Cache HIT - 1 ms
INFO AsyncMoviesService : Page 3: Fetched from API - 424 ms
```

**Enable detailed adapter logging (DEBUG level):**
```yaml
logging:
  level:
    com.challenge.movies: DEBUG
```

This will show additional cache adapter operations:
```
DEBUG RedisCacheAdapter : Cache HIT for key: movies:page:1
DEBUG RedisCacheAdapter : Cached value in Redis for key: movies:page:2 with TTL: 1 minutes
```

**Redis Commander UI:**
- Access http://localhost:8081 to visually inspect cached keys
- View TTL, value size, and expiration times
- Manually clear cache if needed

---

## 🚦 Rate Limiting

The API implements **global rate limiting** using Bucket4j to protect against abuse and ensure fair usage.

### Configuration

**Default limits:**
```yaml
rate-limit:
  capacity: 10           # Maximum 10 requests
  duration-minutes: 1    # Per 1 minute
```

**How it works:**
- Uses **token bucket algorithm** for smooth rate limiting
- All requests to `/api/**` endpoints are rate-limited
- Tokens refill automatically after the time window expires
- Returns HTTP 429 when limit is exceeded

### Response Behavior

**When limit is NOT exceeded:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "directors": ["Martin Scorsese", "Woody Allen"]
}
```

**When limit IS exceeded:**
```
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later."
}
```

### Testing Rate Limiting

**Test with multiple requests:**
```bash
# Send 15 requests rapidly (exceeds limit of 10)
for i in {1..15}; do
  echo "Request $i:"
  curl -w "\nHTTP Status: %{http_code}\n\n" http://localhost:8080/api/directors?threshold=4
done
```

**Expected result:**
- First 10 requests: HTTP 200 OK
- Requests 11-15: HTTP 429 Too Many Requests
- After 1 minute: Tokens refill, requests work again

### Customization

**Increase rate limit for production:**
```yaml
rate-limit:
  capacity: 100          # 100 requests
  duration-minutes: 1    # per minute
```

**Hourly rate limit:**
```yaml
rate-limit:
  capacity: 1000         # 1000 requests
  duration-minutes: 60   # per hour
```

### Implementation Details

**Location:** `src/main/java/com/challenge/movies/infrastructure/interceptor/RateLimitInterceptor.java`

**Technology:** Bucket4j 8.10.1 with token bucket algorithm

**Scope:** Global (shared across all users)

**Note:** For production environments with multiple instances, consider implementing distributed rate limiting using Redis.

---

## 🔐 Authentication (OAuth2)

The API supports **OAuth2 Client Credentials** authentication with profile-based configuration:
- **Local profile**: Authentication disabled (easier development)
- **Production profile**: OAuth2 Resource Server with JWT validation

### Profile Configuration

**Local (development):**
```bash
# Run with local profile (no authentication)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
All endpoints are publicly accessible without tokens.

**Production:**
```bash
# Run with production profile (OAuth2 enabled)
mvn spring-boot:run
```
Requires valid JWT token for `/api/**` endpoints.

### OAuth2 Configuration

**application.yml (production):**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${OAUTH2_ISSUER_URI}
          jwk-set-uri: ${OAUTH2_JWK_SET_URI}
```

**Environment variables:**
```bash
export OAUTH2_ISSUER_URI=https://your-auth-server.com/realms/your-realm
export OAUTH2_JWK_SET_URI=https://your-auth-server.com/realms/your-realm/protocol/openid-connect/certs
```

### Testing with Keycloak (Local)

**1. Start Keycloak (auto-imports configuration):**
```bash
cd src/main/resources/docker
docker-compose up keycloak
```

Keycloak starts on http://localhost:8180 and **automatically imports** the pre-configured realm:
- **Realm:** `movies-api`
- **Client ID:** `movies-client`
- **Client Secret:** `movies-secret-2024` (for development only!)
- **Grant Type:** Client Credentials
- **Token Lifespan:** 5 minutes

**2. Start API in production mode:**
```bash
# In a new terminal
mvn spring-boot:run
```

**3. Get Access Token (using curl):**
```bash
curl -X POST http://localhost:8180/realms/movies-api/protocol/openid-connect/token \
  -d "client_id=movies-client" \
  -d "client_secret=movies-secret-2024" \
  -d "grant_type=client_credentials"
```

**4. Make authenticated request:**
```bash
# Extract token from previous response and use it
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  http://localhost:8080/api/directors?threshold=4
```

**Or use the [Postman Collection](#-postman-collection) for easier testing!**

### Verify Keycloak Configuration (Optional)

Access Keycloak Admin Console to verify the auto-imported configuration:
- URL: http://localhost:8180
- Username: `admin`
- Password: `admin`
- Navigate to: Realm `movies-api` → Clients → `movies-client` → Credentials tab

### Public Endpoints

These endpoints are always accessible without authentication:
- `/actuator/health` - Health check
- `/actuator/info` - Application info
- `/swagger-ui/**` - API documentation
- `/v3/api-docs/**` - OpenAPI specification

### Security Adapter (Domain Access)

If your domain needs authentication context, use the `SecurityPort`:

```java
@Service
@RequiredArgsConstructor
public class SomeService {

  private final SecurityPort securityPort;

  public void doSomething() {
    if (securityPort.isAuthenticated()) {
      String clientId = securityPort.getCurrentClientId().orElse("anonymous");
      List<String> scopes = securityPort.getCurrentScopes();
      // Use authentication info...
    }
  }
}
```

**Implementation:**
- Port: `src/main/java/com/challenge/movies/domain/port/SecurityPort.java`
- Adapter: `src/main/java/com/challenge/movies/infrastructure/adapter/security/SecurityContextAdapter.java`

---

## 📮 Postman Collection

Ready-to-use Postman collection with **zero manual configuration needed** for local testing!

### 📥 Import Collection

**File:** [`postman/Movies API.postman_collection.json`](postman/Movies%20API.postman_collection.json)

**Import steps:**
1. Open Postman
2. Click **Import** button
3. Drag and drop `Movies API.postman_collection.json` or select it
4. Collection imports with all pre-configured settings ✅

### 📂 Collection Structure

The collection is organized into **two folders** for different environments:

#### 📁 Local Profile (No Auth)
For development with authentication disabled.

**Included requests:**
- Get Directors (Threshold 4)
- Get Directors (Threshold 0)
- Health Check
- Swagger UI

**Usage:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
Then use any request - **no token needed!**

#### 📁 Production Profile (OAuth2)
For testing with OAuth2 authentication enabled.

**Included requests:**
1. **Get Access Token** ← Run this first!
2. **Get Directors (Threshold 4)** - Authenticated
3. **Get Directors (Threshold 0)** - Authenticated
4. **Test Without Token** - Should fail with 401
5. **Health Check (Public)** - Always accessible

### 🚀 Quick Start Guide

#### Option 1: Local Profile (No Authentication)

```bash
# 1. Start API
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 2. In Postman: Use any request from "Local Profile" folder
```

No token, no OAuth2, no Keycloak needed! Perfect for quick testing.

#### Option 2: Production Profile (OAuth2)

```bash
# 1. Start Keycloak (auto-imports realm with client_secret)
cd src/main/resources/docker
docker-compose up keycloak

# 2. Start API in production mode (new terminal)
mvn spring-boot:run

# 3. In Postman: Execute "Production Profile > 1. Get Access Token"
#    ✅ Token is automatically saved to {{access_token}} variable

# 4. Use any other request in the folder
#    Token is automatically included in requests!
```

### 🔑 Pre-configured OAuth2 Client

The Postman collection already includes the OAuth2 credentials:

| Variable | Value | Description |
|----------|-------|-------------|
| `client_id` | `movies-client` | OAuth2 client identifier |
| `client_secret` | `movies-secret-2024` | Pre-configured secret (dev only!) |
| `access_token` | (auto-saved) | Token saved after "Get Access Token" |

**No manual configuration needed!** The Keycloak realm is auto-imported with these credentials.

### ✨ Smart Features

**Auto-save Token:**
When you execute "1. Get Access Token", a script automatically:
- ✅ Extracts `access_token` from response
- ✅ Saves it to collection variable `{{access_token}}`
- ✅ Makes it available for all subsequent requests
- ✅ Logs confirmation in Postman Console

**Pre-configured URLs:**
- API: `http://localhost:8080`
- Keycloak: `http://localhost:8180`
- Realm: `movies-api`

### 🔧 Customization

To use different OAuth2 credentials:

1. Right-click on the collection → **Edit**
2. Go to **Variables** tab
3. Update `client_secret` value
4. Save changes

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

### application.yml (Production - Redis)

```yaml
movies:
  api:
    url: https://wiremock.dev.eroninternational.com
  async:
    core-pool-size: 5
    max-pool-size: 10
    queue-capacity: 50
    thread-name-prefix: MoviesAsync-

cache:
  ttl-minutes: 1              # Cache TTL (1 minute for demo)
  max-size: 1000              # Max cache entries (Caffeine only)
  key-prefix: "movies:page:"  # Configurable cache key prefix

rate-limit:
  capacity: 10                # Max requests allowed
  duration-minutes: 1         # Time window in minutes

spring:
  application:
    name: movies
  cloud:
    compatibility-verifier:
      enabled: false
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    tags-sorter: alpha
    operations-sorter: alpha

logging:
  level:
    com.challenge.movies: INFO
```

### application-local.yml (Development - Caffeine)

```yaml
cache:
  ttl-minutes: 1              # Cache TTL (1 minute for demo)
  max-size: 1000              # Max cache entries
  key-prefix: "movies:page:"  # Configurable cache key prefix

spring:
  data:
    redis:
      enabled: false

logging:
  level:
    com.challenge.movies: DEBUG
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

**Change cache TTL:**
```yaml
cache:
  ttl-minutes: 30  # 30 minutes for production
```

**Change cache key prefix:**
```yaml
cache:
  key-prefix: "myapp:movies:"  # Custom prefix
```

---

## 🐳 Docker Setup

### Redis with Docker Compose

The project includes a Docker Compose configuration for running Redis locally with a management UI.

**Location:** `src/main/resources/docker/docker-compose.yml`

**Services included:**
- **Redis 7.2 Alpine** - Lightweight Redis cache server
- **Redis Commander** - Web UI for managing and inspecting Redis

**Start Redis:**
```bash
cd src/main/resources/docker
docker-compose up -d
```

**Stop Redis:**
```bash
docker-compose down
```

**Clear cache:**
```bash
docker exec movies-redis redis-cli FLUSHALL
```

**Access Redis Commander:**
- URL: http://localhost:8081
- View cached keys, TTL, and values visually
- Monitor cache performance in real-time

**Check Redis status:**
```bash
docker-compose ps
```

**View logs:**
```bash
docker-compose logs -f redis
```

For detailed Docker setup instructions, see `src/main/resources/docker/README.md`

---

## 🧪 Testing

**Test Suite:** 51 tests (43 unit tests + 8 integration tests)

**Run tests:**
```bash
mvn test
```

**Code Coverage (JaCoCo):**
- Coverage report: `target/site/jacoco/index.html`
- Thresholds: 80% line coverage, 75% branch coverage
- Excluded: Generated MapStruct classes, DTOs, configuration classes

**Integration Tests:**
- Profile: `application-test.yml` (Redis disabled, OAuth2 disabled)
- Uses `@MockitoBean` to mock external dependencies
- Cache cleared between tests with `@AfterEach`

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
