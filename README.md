# Movies API Challenge

REST API to retrieve directors with movie count above a given threshold. This API fetches movies from an external source and filters directors based on the number of movies they have directed.

## 🚀 Quick Links

| Resource | Link | Description |
|----------|------|-------------|
| 🐳 **Docker Setup** | [`docker-compose.yml`](src/main/resources/docker/docker-compose.yml) | Redis + Keycloak auto-import |
| 📮 **Postman Collection** | [`Download`](postman/Movies%20API.postman_collection.json) | Pre-configured OAuth2 authentication (zero setup!) |
| 📖 **API Documentation** | http://localhost:8080/swagger-ui.html | Interactive Swagger UI |
| 🗄️ **Redis Commander** | http://localhost:8081 | Redis cache management UI |
| 🔑 **Keycloak Admin** | http://localhost:8180 | admin / admin |

## 🎯 Overview

This application consumes an external Movies API and provides an endpoint to retrieve directors who have directed more than a specified threshold number of movies. The results are returned in alphabetical order.

**Key Features:**
- ✅ Hexagonal Architecture (Ports & Adapters)
- ✅ Parallel HTTP requests for improved performance (80%+ faster)
- ✅ Multi-profile caching with configurable TTL (Caffeine for local, Redis for production)
- ✅ Rate limiting with Bucket4j (configurable requests per minute)
- ✅ OAuth2 Client Credentials authentication (disabled in local, enabled in prod)
- ✅ **Ready-to-use Postman Collection** with pre-configured OAuth2
- ✅ Docker Compose setup for Redis, Keycloak with auto-import realm
- ✅ Detailed performance logging and cache monitoring
- ✅ OpenAPI/Swagger documentation
- ✅ Code quality tools (Spotless, PMD, SpotBugs, Checkstyle, JaCoCo)
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

1. **OAuth2 Authentication** - Profile-based security (disabled in local, enabled in production) with JWT validation
2. **Profile-Based Cache** - Caffeine for local development (fast, no infrastructure), Redis for production (distributed, scalable)
3. **Async with Thread Pool** - Configurable thread pool for parallel API calls with proper resource management
4. **Rate Limiting** - Global request limiting with Bucket4j to protect against abuse
5. **Configurable via Properties** - All configurations externalized to `application.yml`

---

## 🛠️ Technologies

- **Java 21**
- **Spring Boot 3.5.7**
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
- JaCoCo (Code coverage)

---

## 🚀 Getting Started

### Prerequisites

- Java 21
- Maven 3.8+
- Docker (optional, for Redis and Keycloak)

### Local Development (Recommended)

Run with **local profile** for quick development (no Docker needed):

```bash
# Clone and navigate
git clone <repository-url>
cd movies-api

# Run with local profile (Caffeine cache, no OAuth2)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Access:**
- API: http://localhost:8080/api/directors?threshold=4
- Swagger UI: http://localhost:8080/swagger-ui.html

### Production Mode

Run with **production profile** (Redis cache + OAuth2):

```bash
# 1. Start infrastructure (Redis + Keycloak)
cd src/main/resources/docker
docker-compose up -d

# 2. Run application (in project root)
mvn spring-boot:run
```

**Access with authentication:**
- Use [Postman Collection](postman/Movies%20API.postman_collection.json) for OAuth2 flow
- Or get token manually from Keycloak at http://localhost:8180

### Build Options

**Build JAR:**
```bash
mvn clean package
```

**Build without tests (faster):**
```bash
mvn clean package -DskipTests
```

**Run JAR:**
```bash
java -jar target/movies-0.0.1-SNAPSHOT.jar
```

**Run with specific profile:**
```bash
java -jar target/movies-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

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

| Status | Description |
|--------|-------------|
| **200 OK** | Successfully retrieved directors list |
| **400 Bad Request** | Invalid threshold parameter (negative, missing, or non-numeric) |
| **429 Too Many Requests** | Rate limit exceeded |
| **500 Internal Server Error** | Unexpected error occurred |

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

**JaCoCo** - Code coverage
- 80% line coverage threshold
- 75% branch coverage threshold
- Report: `target/site/jacoco/index.html`

---

## ⚙️ Configuration

### application.yml (Production - Redis + OAuth2)

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
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${OAUTH2_ISSUER_URI:http://localhost:8180/realms/movies-api}
          jwk-set-uri: ${OAUTH2_JWK_SET_URI:http://localhost:8180/realms/movies-api/protocol/openid-connect/certs}

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    tags-sorter: alpha
    operations-sorter: alpha

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    com.challenge.movies: INFO
```

### application-local.yml (Development - Caffeine + No Auth)

```yaml
cache:
  ttl-minutes: 1              # Cache TTL (1 minute for demo)
  max-size: 1000              # Max cache entries
  key-prefix: "movies:page:"  # Configurable cache key prefix

rate-limit:
  capacity: 10                # Max requests allowed
  duration-minutes: 1         # Time window in minutes

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

The project includes a Docker Compose configuration with all infrastructure dependencies.

**Location:** `src/main/resources/docker/docker-compose.yml`

**Services included:**
- **Redis 7.2 Alpine** (port 6379) - Distributed cache with persistence
- **Redis Commander** (port 8081) - Web UI for Redis management
- **Keycloak 23.0** (port 8180) - OAuth2 authorization server with auto-import realm

### Start All Services

```bash
cd src/main/resources/docker
docker-compose up -d
```

### Start Individual Services

```bash
# Only Redis + Redis Commander
docker-compose up -d redis redis-commander

# Only Keycloak
docker-compose up -d keycloak
```

### Stop Services

```bash
docker-compose down
```

### Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Redis Commander** | http://localhost:8081 | No auth required |
| **Keycloak Admin Console** | http://localhost:8180 | admin / admin |
| **Redis CLI** | `docker exec -it movies-redis redis-cli` | - |

### Useful Commands

**Clear Redis cache:**
```bash
docker exec movies-redis redis-cli FLUSHALL
```

**Check services status:**
```bash
docker-compose ps
```

**View logs:**
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f redis
docker-compose logs -f keycloak
```

**Stop and remove volumes (clean slate):**
```bash
docker-compose down -v
```

### Keycloak Auto-Import

Keycloak automatically imports the pre-configured realm on startup:
- **Realm:** `movies-api`
- **Client ID:** `movies-client`
- **Client Secret:** `movies-secret-2024`
- **Configuration:** `src/main/resources/docker/keycloak/realm-export.json`

