# Smart Parking Management System — Architecture

## 1. Architecture Overview

The Smart Parking Management System follows a layered Spring Boot architecture.

```text
Client
   |
   | HTTP / JSON
   v
+--------------------+
| Controller Layer   |
+--------------------+
          |
          v
+--------------------+
| Service Layer      |
+--------------------+
          |
          v
+--------------------+
| Repository Layer   |
+--------------------+
          |
          v
+--------------------+
| JPA / Hibernate    |
+--------------------+
          |
          v
+--------------------+
| MySQL              |
+--------------------+
```

The application separates API handling, business logic, persistence and database concerns.

---

# 2. Package Architecture

```text
com.airtribe.smartparking
│
├── common
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── mapper
├── repository
├── service
└── SmartParkingSystemApplication
```

## Package Responsibilities

### controller

Handles incoming HTTP requests.

Responsibilities:

- Receive requests
- Validate request DTOs
- Call service-layer operations
- Return standardized API responses

Controllers should not contain core business logic.

---

### service

Contains business logic and application use cases.

Important operations include:

```text
checkIn()
checkOut()
getParkingAvailability()
allocateParkingSpot()
createParkingTicket()
calculateParkingFee()
recordPayment()
releaseParkingSpot()
```

`ParkingServiceImpl` acts as the main orchestration service for parking operations.

---

### repository

Provides database access using Spring Data JPA.

Examples include repositories for:

```text
Vehicle
ParkingFloor
ParkingSpot
ParkingTicket
Payment
```

Spring Data generates many queries automatically from repository method names.

Custom locking behavior is used where concurrency control is required.

---

### entity

Contains JPA persistence entities:

```text
Vehicle
ParkingFloor
ParkingSpot
ParkingTicket
Payment
```

These classes represent the relational domain model stored in MySQL.

---

### dto

Contains API request and response objects.

Example request DTOs:

```text
CheckInRequest
CheckOutRequest
```

Example response DTOs:

```text
ParkingAvailabilityResponse
ParkingTicketResponse
PaymentResponse
```

DTOs prevent persistence entities from becoming the public API contract.

---

### mapper

Handles conversion between entities and DTOs.

The project uses ModelMapper as well as dedicated mapping logic where required.

---

### exception

Contains domain-specific exceptions and centralized exception handling.

`GlobalExceptionHandler` uses:

```java
@RestControllerAdvice
```

to translate exceptions into appropriate HTTP responses.

---

### config

Contains Spring configuration such as OpenAPI and mapping configuration.

---

# 3. Check-In Architecture

The check-in request enters through the REST controller.

```text
POST /api/v1/parking/check-in
              |
              v
       ParkingController
              |
              v
       ParkingServiceImpl
              |
       @Transactional
              |
     +--------+---------+
     |                  |
     v                  v
VehicleService   ParkingSpotService
     |                  |
     v                  v
VehicleRepository   ParkingSpotRepository
                        |
                        |
               PESSIMISTIC_WRITE
                        |
                        v
                Available Spot
                        |
                        v
              ParkingTicketService
                        |
                        v
             ParkingTicketRepository
```

## Check-In Sequence

```text
1. Receive CheckInRequest
        |
2. Validate request
        |
3. Verify vehicle is not already parked
        |
4. Find existing vehicle or create vehicle
        |
5. Determine compatible parking spot type
        |
6. Find and lock an AVAILABLE parking spot
        |
7. Mark parking spot OCCUPIED
        |
8. Generate ticket number
        |
9. Create ACTIVE ParkingTicket
        |
10. Map entity to ParkingTicketResponse
        |
11. Commit transaction
        |
12. Return response
```

---

# 4. Why Check-In Is Transactional

Check-in changes multiple pieces of state.

For example:

```text
ParkingSpot
AVAILABLE → OCCUPIED

+

ParkingTicket
new ACTIVE ticket
```

These changes should succeed together.

Therefore the main check-in operation uses:

```java
@Transactional
```

Conceptually:

```text
BEGIN TRANSACTION

Allocate spot
Update spot
Create ticket

Everything successful?
        |
   +----+----+
   |         |
  YES        NO
   |         |
COMMIT    ROLLBACK
```

If ticket creation fails after changing the parking spot, the transaction can roll back the changes rather than leaving the spot incorrectly occupied.

---

# 5. Concurrent Check-In Problem

Consider two vehicles arriving simultaneously when only one suitable parking spot is available.

Without locking:

```text
Thread A                  Thread B

Read C-101 AVAILABLE      Read C-101 AVAILABLE
        |                         |
        v                         v
Assign C-101               Assign C-101

             WRONG
```

Both requests could attempt to allocate the same spot.

---

# 6. Pessimistic Locking

The parking-spot allocation query uses:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

Conceptually the database performs:

```sql
SELECT ...
FROM parking_spot
WHERE spot_type = ?
  AND status = ?
ORDER BY id
LIMIT 1
FOR UPDATE;
```

The selected database row remains locked for the transaction.

Example:

```text
Thread A
   |
Lock C-101
   |
C-101 → OCCUPIED
   |
COMMIT
   |
Release Lock


Thread B
   |
Wait
   |
Thread A commits
   |
Search AVAILABLE spot again
   |
Gets C-102
```

This prevents two concurrent allocation transactions from selecting the same available parking spot.

---

# 7. Optimistic Locking

`ParkingSpot` also contains:

```java
@Version
private Long version;
```

Hibernate uses this version value when updating the entity.

Conceptually:

```text
C-101

status  = AVAILABLE
version = 1
```

An update can become similar to:

```sql
UPDATE parking_spot
SET status = 'OCCUPIED',
    version = 2
WHERE id = ?
AND version = 1;
```

If another transaction already changed the row, the expected version no longer matches.

This allows Hibernate to detect stale updates.

For this project:

```text
PESSIMISTIC_WRITE
        |
        +--> protects parking allocation

@Version
        |
        +--> protects against stale updates
```

---

# 8. Check-Out Architecture

```text
POST /api/v1/parking/check-out
              |
              v
       ParkingController
              |
              v
       ParkingServiceImpl
              |
       @Transactional
              |
              v
     Find ACTIVE Ticket
              |
              v
        Close Ticket
              |
              v
       Calculate Fee
              |
              v
       Update Ticket
              |
              v
       Record Payment
              |
              v
    Payment SUCCESS?
        /           \
      YES            NO
       |              |
       v              |
 Release Spot         |
       |              |
       +------+-------+
              |
              v
       PaymentResponse
```

---

# 9. Check-Out Transaction

Checkout can modify:

```text
ParkingTicket
Payment
ParkingSpot
```

The operation therefore executes within one transaction.

Example:

```text
BEGIN

Find ACTIVE ticket

ACTIVE
   ↓
COMPLETED

Calculate fee

Save totalFee

Insert Payment

If SUCCESS:
    OCCUPIED
        ↓
    AVAILABLE

COMMIT
```

If an unchecked exception occurs before successful completion:

```text
ROLLBACK
```

This helps protect database consistency.

---

# 10. Parking Availability Flow

Availability is a read-only operation.

```text
GET /api/v1/parking/availability
             |
             v
      ParkingController
             |
             v
      ParkingServiceImpl
             |
 @Transactional(readOnly = true)
             |
             v
     ParkingSpotService
             |
             v
    ParkingSpotRepository
             |
             v
           MySQL
```

The response reports available spots grouped by parking-spot type.

---

# 11. Entity Relationship Architecture

```text
ParkingFloor
     |
     | 1:N
     v
ParkingSpot
     |
     | 1:N over time
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

### Vehicle → ParkingTicket

A vehicle can visit the parking lot multiple times.

Therefore:

```text
Vehicle 1 : N ParkingTicket
```

---

### ParkingFloor → ParkingSpot

A floor contains multiple parking spots.

```text
ParkingFloor 1 : N ParkingSpot
```

---

### ParkingSpot → ParkingTicket

A parking spot can be used by many parking sessions over time.

```text
ParkingSpot 1 : N ParkingTicket
```

Each individual ticket references one parking spot.

---

### ParkingTicket → Payment

The current simplified payment model uses:

```text
ParkingTicket 1 : 1 Payment
```

The payment table enforces uniqueness on its parking-ticket foreign key.

---

# 12. Fetch Strategy

Relationships use lazy loading where appropriate.

Example:

```java
@ManyToOne(fetch = FetchType.LAZY)
```

and:

```java
@OneToMany(fetch = FetchType.LAZY)
```

This avoids automatically loading related data when it is not required.

The application does not solve potential N+1 problems by switching everything to `EAGER`.

Instead, fetch behavior should be optimized for specific use cases when required.

---

# 13. N+1 Example

Consider:

```java
List<Vehicle> vehicles = vehicleRepository.findAll();

for (Vehicle vehicle : vehicles) {
    vehicle.getParkingTickets().size();
}
```

This could generate:

```text
1 query  → retrieve vehicles

N queries → retrieve tickets for each vehicle
```

Result:

```text
1 + N queries
```

If such a use case is introduced, it can be optimized with an explicit fetch strategy such as a fetch join or entity graph.

The current core APIs do not require loading every vehicle together with its entire ticket history.

---

# 14. Open EntityManager in View

The application disables Open EntityManager in View:

```properties
spring.jpa.open-in-view=false
```

Database access and lazy relationship initialization should therefore occur within the appropriate service/transaction boundary rather than being accidentally triggered during HTTP response serialization.

The API uses DTOs rather than exposing JPA entities directly.

---

# 15. DTO Architecture

The application separates:

```text
API Model
   |
   | DTO
   v
Business Layer
   |
   v
Persistence Entity
```

This prevents API consumers from becoming tightly coupled to database entities.

It also avoids issues such as recursive serialization of bidirectional JPA relationships.

---

# 16. Exception Architecture

```text
Application Exception
        |
        v
GlobalExceptionHandler
        |
        v
HTTP Status + ErrorResponse
```

The global handler uses:

```java
@RestControllerAdvice
```

and:

```java
@ExceptionHandler
```

to translate Java exceptions into API responses.

Examples:

```text
ResourceNotFoundException
        → 404

VehicleAlreadyParkedException
        → 409

ParkingSpotNotAvailableException
        → 409

Validation Failure
        → 400

Unexpected Exception
        → 500
```

---

# 17. Database Indexing Strategy

Indexes are based on actual query patterns.

### Parking Spot Allocation

Frequent query:

```text
spot_type + status
```

Composite index:

```text
(spot_type, status)
```

### Active Ticket Lookup

Frequent query:

```text
vehicle_id + status
```

Composite index:

```text
(vehicle_id, status)
```

Unique values such as:

```text
vehicle_number
spot_number
ticket_number
```

are protected by unique constraints.

The project avoids adding indexes indiscriminately because indexes also add storage and write-maintenance costs.

---

# 18. Configuration Architecture

Common configuration:

```text
application.properties
```

Development configuration:

```text
application-dev.properties
```

Test configuration:

```text
application-test.properties
```

Database configuration is supplied through:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

This prevents database credentials from being hard-coded into application source configuration.

---

# 19. Docker Architecture

The complete application can run through Docker Compose.

```text
                 HOST
                  |
                  | :8080
                  v
      +--------------------------+
      | smart-parking-app        |
      | Spring Boot / Java 21    |
      +------------+-------------+
                   |
                   |
                   | JDBC
                   | mysql:3306
                   v
      +--------------------------+
      | smart-parking-mysql      |
      | MySQL                    |
      +------------+-------------+
                   |
                   v
            Persistent Volume
```

Inside Docker, the Spring Boot application does not connect to:

```text
localhost:3306
```

for MySQL.

It uses Docker Compose service discovery:

```text
mysql:3306
```

---

# 20. Testing Architecture

The project uses two primary testing levels.

## Unit Testing

```text
Service Under Test
      |
      +---- mocked dependency
      |
      +---- mocked repository/service
```

JUnit 5 and Mockito test business behavior in isolation.

## Integration Testing

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
Test Database
```

Integration tests verify that application layers work together correctly.

---

# 21. Key Architectural Decisions

### Why DTOs instead of exposing entities?

To separate the API contract from persistence and prevent JPA serialization problems.

### Why `LAZY` relationships?

To avoid retrieving related data unless the use case requires it.

### Why pessimistic locking?

Parking-spot allocation is concurrency-sensitive and two check-in requests must not allocate the same spot.

### Why `@Version`?

To detect stale updates to mutable parking-spot records.

### Why `@Transactional` at the orchestration service?

Check-in and check-out modify multiple pieces of related state that should succeed or fail together.

### Why environment variables?

Database credentials differ between environments and should not be hard-coded into source-controlled configuration.

### Why Docker Compose?

The application depends on both Spring Boot and MySQL. Compose provides repeatable container configuration, networking and database persistence.

---

# 22. Overall Request Lifecycle

```text
HTTP Request
     |
     v
Controller
     |
     | request validation
     v
Service
     |
     | business rules
     | transaction boundary
     v
Repository
     |
     | JPA
     v
Hibernate
     |
     | SQL
     v
MySQL
     |
     v
Entity
     |
     | Mapper
     v
Response DTO
     |
     v
Standard API Response
     |
     v
HTTP Response
```

This architecture keeps API handling, business logic and persistence responsibilities separated while supporting validation, transactions, concurrency control, testing and containerized deployment.