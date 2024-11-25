# Ping-Pong Microservices

## Overview
This project demonstrates a distributed microservices system with rate-limited communication between Ping and Pong services using Spring WebFlux.

## Features
- Rate limiting at both service and global levels
- File-based distributed locking mechanism
- Reactive programming with Spring WebFlux
- Comprehensive logging

## Prerequisites
- Java 17+
- Maven

## Running the Services
1. Start Pong Service:
```bash
cd pong-service
mvn spring-boot:run
```

2. Start Ping Service:
```bash
cd ping-service
mvn spring-boot:run
```

## Rate Limiting Details
- Pong Service: 1 request per second
- Global Rate Limit: 2 requests per second across all Ping processes

## Logging
Each service logs its interactions in a separate log file:
- `ping-service.log`
- `pong-service.log`

## Testing
Run unit tests with:
```bash
mvn test
```

## Coverage
Jacoco is configured to ensure ≥80% test coverage.
