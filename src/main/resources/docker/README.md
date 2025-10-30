# Docker Setup for Movies API

This directory contains Docker Compose configuration for running Redis locally.

## Prerequisites

- Docker Desktop installed and running
- Port 6379 available (Redis)
- Port 8081 available (Redis Commander UI)

## Services

### Redis
- **Version**: 7.2-alpine
- **Port**: 6379
- **Persistence**: Enabled with AOF (Append Only File)
- **Container Name**: movies-redis

### Redis Commander (Web UI)
- **Port**: 8081
- **URL**: http://localhost:8081
- **Purpose**: Visual interface to inspect Redis cache

## Usage

### Start Redis

From the project root directory:

```bash
cd src/main/resources/docker
docker-compose up -d
```

Or from anywhere:

```bash
docker-compose -f src/main/resources/docker/docker-compose.yml up -d
```

### Check Status

```bash
docker-compose ps
```

### View Logs

```bash
# All services
docker-compose logs -f

# Only Redis
docker-compose logs -f redis
```

### Stop Redis

```bash
docker-compose down
```

### Stop and Remove Data

```bash
docker-compose down -v
```

## Verify Redis is Running

### Using Redis CLI

```bash
docker exec -it movies-redis redis-cli ping
# Should return: PONG
```

### Using Redis Commander

Open browser: http://localhost:8081

You should see the Redis instance and can browse keys.

## Testing with the Application

1. **Start Redis**:
   ```bash
   docker-compose up -d
   ```

2. **Run the application** (default profile uses Redis):
   ```bash
   mvn spring-boot:run
   ```

3. **Make a request**:
   ```bash
   curl http://localhost:8080/api/directors?threshold=4
   ```

4. **Check cache in Redis Commander**:
   - Open http://localhost:8081
   - You should see keys like `movies:page:1`, `movies:page:2`, etc.

5. **Make the same request again** - should be much faster (served from cache)

## Troubleshooting

### Port 6379 already in use

```bash
# Find process using port 6379
netstat -ano | findstr :6379

# Or stop existing Redis
docker stop movies-redis
```

### Connection refused

Check if Redis is healthy:
```bash
docker-compose ps
```

Check application.yml has correct Redis host:
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Clear all cache

```bash
docker exec -it movies-redis redis-cli FLUSHALL
```
