# Smart Parking Management System

A backend REST API for managing parking-lot operations such as vehicle check-in, parking-spot allocation, parking availability, ticket management, fee calculation, payment processing, and vehicle check-out.

The project is built with **Java 21, Spring Boot, Spring Data JPA, Hibernate, MySQL, Maven, Docker, and Swagger/OpenAPI**.

---

## Features

- Check current parking availability
- Vehicle check-in
- Automatic parking-spot allocation
- Parking ticket generation
- Duplicate vehicle check-in prevention
- Vehicle check-out
- Parking fee calculation
- Payment recording
- Automatic parking-spot release after successful payment
- Request validation
- Centralized exception handling
- Transaction management
- Concurrent parking-spot allocation protection
- REST API documentation with Swagger/OpenAPI
- Unit and integration testing
- Dockerized Spring Boot application
- Dockerized MySQL database
- Persistent MySQL storage using Docker volumes
- Environment-based database configuration

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 3.3.5 | Application framework |
| Spring Web | REST APIs |
| Spring Data JPA | Persistence layer |
| Hibernate | ORM |
| MySQL | Relational database |
| Bean Validation | Request validation |
| Lombok | Boilerplate reduction |
| ModelMapper | DTO/entity mapping |
| Springdoc OpenAPI | Swagger/API documentation |
| JUnit 5 | Testing |
| Mockito | Unit testing |
| MockMvc | API integration testing |
| Maven | Build/dependency management |
| Docker | Application containerization |
| Docker Compose | Multi-container orchestration |

---

## Architecture

The application follows a layered architecture:

```text
Client
  |
  v
Controller
  |
  v
Service Layer
  |
  v
Repository Layer
  |
  v
Spring Data JPA / Hibernate
  |
  v
MySQL
```

### Responsibilities

**Controller Layer**

Receives HTTP requests, performs request validation and returns API responses.

**Service Layer**

Contains the business logic and coordinates operations such as check-in and check-out.

**Repository Layer**

Provides database access using Spring Data JPA.

**DTO Layer**

Separates API request/response models from persistence entities.

**Exception Layer**

Provides application-specific exceptions and centralized exception handling.

---

## Core Domain Model

The main entities are:

```text
Vehicle
ParkingFloor
ParkingSpot
ParkingTicket
Payment
```

### Relationships

```text
ParkingFloor
     |
     | 1:N
     v
ParkingSpot
     |
     | 1:N
     v
ParkingTicket
     ^
     | N:1
     |
Vehicle

ParkingTicket
     |
     | 1:1
     v
Payment
```

A vehicle can have multiple parking tickets over time.

A parking floor contains multiple parking spots.

A parking spot can be associated with multiple historical parking tickets.

Each parking ticket has one payment in the current payment model.

---

## Main API Endpoints

Base URL:

```text
http://localhost:8080/api/v1/parking
```

| Method | Endpoint | Description |
|---|---|---|
| GET | `/availability` | Get available parking spots |
| POST | `/check-in` | Check in a vehicle |
| POST | `/check-out` | Check out a vehicle and process payment |

---

## Parking Availability

### Request

```http
GET /api/v1/parking/availability
```

### Example Response

```json
{
  "success": true,
  "message": "Parking availability fetched successfully.",
  "data": {
    "bikeAvailable": 8,
    "compactAvailable": 8,
    "largeAvailable": 8,
    "busAvailable": 4
  }
}
```

---

## Vehicle Check-In

### Request

```http
POST /api/v1/parking/check-in
Content-Type: application/json
```

```json
{
  "vehicleNumber": "MH27BD3354",
  "ownerName": "Amir",
  "vehicleType": "CAR"
}
```

### Check-In Flow

```text
Validate Request
      |
      v
Check Vehicle Already Parked
      |
      v
Find/Create Vehicle
      |
      v
Allocate Parking Spot
      |
      v
Create ACTIVE Parking Ticket
      |
      v
Return Ticket Response
```

The parking-spot allocation operation uses database locking to protect against concurrent allocation of the same spot.

---

## Vehicle Check-Out

### Request

```http
POST /api/v1/parking/check-out
Content-Type: application/json
```

```json
{
  "ticketNumber": "TKT-XXXXXXXXXXXX-XXXXX",
  "vehicleNumber": "MH27BD3354",
  "paymentMode": "UPI",
  "paymentStatus": "SUCCESS"
}
```

### Check-Out Flow

```text
Validate Request
      |
      v
Find ACTIVE Ticket
      |
      v
Close Ticket
      |
      v
Calculate Parking Fee
      |
      v
Save Total Fee
      |
      v
Record Payment
      |
      v
Payment Successful?
      |
      +---- YES ----> Release Parking Spot
      |
      v
Return Payment Response
```

---

## Validation

The API uses Jakarta Bean Validation.

Examples include:

```java
@NotBlank
@NotNull
@Size
@Pattern
```

Invalid requests return HTTP `400 Bad Request`.

Example:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed."
}
```

---

## Exception Handling

Centralized exception handling is implemented using:

```java
@RestControllerAdvice
```

Business exceptions include scenarios such as:

- Resource not found
- Ticket not found
- Vehicle already parked
- Parking spot unavailable
- Payment failure

Examples of HTTP status mappings:

| Scenario | HTTP Status |
|---|---|
| Invalid request | 400 Bad Request |
| Resource/ticket not found | 404 Not Found |
| Vehicle already parked | 409 Conflict |
| Parking spot unavailable | 409 Conflict |
| Unexpected server error | 500 Internal Server Error |

---

## Transaction Management

Check-in and check-out are transactional operations.

```java
@Transactional
```

is used at the service layer so related database modifications execute as one logical unit.

For example, during check-out:

```text
Update Ticket
     +
Calculate Fee
     +
Create Payment
     +
Release Spot
```

If an unchecked exception occurs before completion, the transaction is rolled back.

Read-only operations use:

```java
@Transactional(readOnly = true)
```

where appropriate.

---

## Concurrency Handling

Parking-spot allocation is protected using pessimistic locking:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This prevents concurrent check-in requests from allocating the same available parking spot.

`ParkingSpot` also uses optimistic versioning:

```java
@Version
```

to detect stale updates.

---

## Database Optimization

Indexes are used for frequent lookup patterns.

Examples include:

```text
ParkingSpot:
(spot_type, status)

ParkingTicket:
(vehicle_id, status)
```

Unique constraints are used for values such as:

```text
vehicle_number
spot_number
ticket_number
```

The application uses lazy loading for entity relationships and disables Open EntityManager in View:

```properties
spring.jpa.open-in-view=false
```

API responses use DTOs instead of directly serializing JPA entities.

---

## Swagger / OpenAPI

Swagger UI:

```text
http://localhost:8080/swagger-ui
```

OpenAPI specification:

```text
http://localhost:8080/api-docs
```

Swagger documents:

- Parking availability
- Vehicle check-in
- Vehicle check-out
- Request schemas
- Response codes
- Validation requirements

---

## Configuration

The project uses Spring profiles.

```text
application.properties
application-dev.properties
application-test.properties
```

### Development Environment Variables

The application expects:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Example:

```text
DB_URL=jdbc:mysql://localhost:3306/smart_parking_db
DB_USERNAME=<username>
DB_PASSWORD=<password>
```

Database credentials should not be committed to source control.

---

## Running Locally

### Prerequisites

Install:

- Java 21
- Maven
- MySQL

Configure the required database environment variables.

Then run:

```bash
mvn spring-boot:run
```

Alternatively:

```bash
mvn clean package
java -jar target/smart-parking-system-0.0.1-SNAPSHOT.jar
```

The application starts on:

```text
http://localhost:8080
```

---

## Running with Docker

### Prerequisites

Install:

- Docker
- Docker Compose

Build and start the complete application:

```bash
docker compose up --build
```

Run in detached mode:

```bash
docker compose up -d
```

Check container status:

```bash
docker compose ps
```

View application logs:

```bash
docker compose logs -f smart-parking-app
```

View MySQL logs:

```bash
docker compose logs -f mysql
```

Stop the application:

```bash
docker compose down
```

The MySQL database uses a Docker named volume, so data persists across normal container recreation.

---

## Docker Architecture

```text
Host Machine
     |
     | :8080
     v
+----------------------------+
| smart-parking-app          |
| Spring Boot / Java 21      |
| Container Port: 8080       |
+-------------+--------------+
              |
              | JDBC
              | mysql:3306
              v
+----------------------------+
| smart-parking-mysql        |
| MySQL                      |
| Container Port: 3306       |
+-------------+--------------+
              |
              v
     Docker Named Volume
```

Docker Compose service discovery allows the Spring Boot container to connect to MySQL using the service hostname:

```text
mysql
```

instead of `localhost`.

---

## Testing

The project contains both unit and integration tests.

### Unit Tests

JUnit 5 and Mockito are used to test service-layer business logic in isolation.

### Integration Tests

Integration tests use:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
```

to test the real flow through:

```text
MockMvc
   |
   v
Controller
   |
   v
Service
   |
   v
Repository
   |
   v
Test MySQL Database
```

Covered scenarios include:

- Application context loading
- Parking availability
- Vehicle check-in
- Vehicle check-out
- Request validation
- Vehicle-number validation
- Duplicate vehicle check-in
- Parking spot unavailable
- Business exception handling

Run tests using:

```bash
mvn clean test
```

---

## Project Structure

```text
src
├── main
│   ├── java
│   │   └── com.airtribe.smartparking
│   │       ├── common
│   │       ├── config
│   │       ├── controller
│   │       ├── dto
│   │       ├── entity
│   │       ├── enums
│   │       ├── exception
│   │       ├── mapper
│   │       ├── repository
│   │       ├── service
│   │       └── SmartParkingSystemApplication
│   │
│   └── resources
│
└── test
```

---

## Key Engineering Concepts Demonstrated

This project demonstrates practical usage of:

- Layered backend architecture
- REST API design
- DTO pattern
- Dependency injection
- Bean Validation
- Global exception handling
- JPA entity relationships
- Lazy loading
- Transaction management
- Transaction rollback
- Optimistic locking
- Pessimistic locking
- Database indexing
- N+1 awareness
- Spring Profiles
- Environment variables
- Structured logging
- Unit testing
- Integration testing
- Swagger/OpenAPI
- Docker multi-stage builds
- Docker Compose
- Container networking
- Persistent database volumes

---

## Future Improvements

Potential future enhancements include:

- Authentication and authorization
- Admin APIs for managing floors and parking spots
- Parking reservation
- Multiple parking-lot support
- Advanced pricing strategies
- Payment gateway integration
- Historical reports and analytics
- CI/CD pipeline
- Metrics and monitoring

---

## Build

```bash
mvn clean package
```

Run all tests:

```bash
mvn clean test
```

Run using Docker:

```bash
docker compose up --build
```

---

## License

This project is intended for learning, portfolio development, and demonstration purposes.