# Smart Parking Management System — Database Design

## 1. Overview

The Smart Parking Management System uses **MySQL** as its relational database and **Spring Data JPA / Hibernate** for persistence.

The core database tables are:

```text
vehicles
parking_floors
parking_spot
parking_tickets
payments
```

The design models vehicles entering a parking facility, being assigned parking spots, receiving tickets, completing parking sessions, and making payments.

---

# 2. Entity Relationship Overview

```text
+------------------+
| parking_floors   |
+------------------+
        |
        | 1:N
        v
+------------------+
| parking_spot     |
+------------------+
        |
        | 1:N
        v
+------------------+       N:1       +------------------+
| parking_tickets  |---------------->| vehicles         |
+------------------+                 +------------------+
        |
        | 1:1
        v
+------------------+
| payments         |
+------------------+
```

Relationships:

```text
ParkingFloor  1 : N ParkingSpot

ParkingSpot   1 : N ParkingTicket

Vehicle       1 : N ParkingTicket

ParkingTicket 1 : 1 Payment
```

---

# 3. `vehicles`

Stores vehicle information.

Important fields:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `vehicle_number` | Unique vehicle registration number |
| `owner_name` | Vehicle owner's name |
| `vehicle_type` | Vehicle category |
| audit columns | Inherited from `BaseEntity` |

Example:

```text
id              = 1
vehicle_number  = MH27BD3354
owner_name      = Amir
vehicle_type    = CAR
```

## Constraints

`vehicle_number` is unique.

Conceptually:

```sql
UNIQUE (vehicle_number)
```

This prevents duplicate vehicle records for the same registration number.

---

# 4. `parking_floors`

Represents individual floors of the parking facility.

Important fields:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `floor_number` | Unique floor number |
| `floor_name` | Human-readable floor name |
| `status` | Current floor status |
| audit columns | Inherited from `BaseEntity` |

Example:

```text
id            = 1
floor_number  = 1
floor_name    = Ground Floor
status        = ACTIVE
```

`floor_number` has a unique constraint.

---

# 5. `parking_spot`

Represents individual parking spaces.

Important fields:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `spot_number` | Unique spot identifier |
| `spot_type` | Parking spot category |
| `status` | Availability state |
| `floor_id` | Foreign key to parking floor |
| `version` | Optimistic-locking version |
| audit columns | Inherited from `BaseEntity` |

Example:

```text
id           = 10
spot_number  = C-101
spot_type    = COMPACT
status       = AVAILABLE
floor_id     = 1
version      = 3
```

## Relationship

```text
parking_floors
      1
      |
      | N
      v
parking_spot
```

Each parking spot belongs to exactly one parking floor.

---

# 6. Parking Spot Types

The application uses enum-based parking-spot types such as:

```text
BIKE
COMPACT
LARGE
BUS
```

Enums are persisted using string representation rather than ordinal numbers.

Example:

```java
@Enumerated(EnumType.STRING)
```

This is preferable to storing enum ordinals such as:

```text
0
1
2
3
```

because changing enum ordering could otherwise change the meaning of persisted values.

---

# 7. Parking Spot Status

A parking spot maintains its current availability state.

The important business transition is:

```text
AVAILABLE
    |
    | check-in
    v
OCCUPIED
    |
    | successful checkout
    v
AVAILABLE
```

This state is used during parking-spot allocation.

---

# 8. `parking_tickets`

Represents an individual parking session.

Important fields:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `ticket_number` | Unique ticket identifier |
| `entry_time` | Vehicle entry time |
| `exit_time` | Vehicle exit time |
| `total_fee` | Calculated parking fee |
| `status` | Ticket status |
| `vehicle_id` | Foreign key to vehicle |
| `parking_spot_id` | Foreign key to parking spot |
| audit columns | Inherited from `BaseEntity` |

Example active ticket:

```text
ticket_number   = TKT-1784901267717-9B08F
entry_time      = ...
exit_time       = NULL
total_fee       = NULL
status          = ACTIVE
vehicle_id      = 1
parking_spot_id = 10
```

After checkout:

```text
status     = COMPLETED
exit_time  = populated
total_fee  = calculated
```

---

# 9. Vehicle–Ticket Relationship

A vehicle may enter the parking facility multiple times.

Therefore:

```text
Vehicle
   1
   |
   | N
   v
ParkingTicket
```

Example:

```text
Vehicle MH27BD3354

    |
    +---- Ticket 001 → COMPLETED
    |
    +---- Ticket 015 → COMPLETED
    |
    +---- Ticket 029 → ACTIVE
```

This preserves parking history rather than creating a new vehicle record for every visit.

---

# 10. ParkingSpot–Ticket Relationship

One parking spot can participate in many parking sessions over time.

```text
ParkingSpot C-101

    |
    +---- Ticket 001
    |
    +---- Ticket 037
    |
    +---- Ticket 081
```

Each individual ticket references one parking spot.

Therefore:

```text
ParkingSpot 1 : N ParkingTicket
```

over the lifetime of the system.

---

# 11. Ticket Status

The main ticket lifecycle is:

```text
ACTIVE
   |
   | checkout
   v
COMPLETED
```

During check-in:

```text
status = ACTIVE
```

During checkout:

```text
status = COMPLETED
```

The application searches for the active parking session using the vehicle and ticket status.

---

# 12. `payments`

Stores payment information associated with parking tickets.

Important fields:

| Column | Purpose |
|---|---|
| `id` | Primary key |
| `amount` | Parking fee paid |
| `payment_mode` | Payment method |
| `payment_status` | Payment result |
| `paid_at` | Payment timestamp |
| `parking_ticket_id` | Associated ticket |
| audit columns | Inherited from `BaseEntity` |

Example:

```text
amount             = 120.00
payment_mode       = UPI
payment_status     = SUCCESS
paid_at            = ...
parking_ticket_id  = 15
```

---

# 13. Ticket–Payment Relationship

The current project uses:

```text
ParkingTicket 1 : 1 Payment
```

The payment table contains:

```text
parking_ticket_id
```

with a unique constraint.

Conceptually:

```sql
UNIQUE (parking_ticket_id)
```

This prevents multiple payment rows from being associated with the same ticket in the current simplified payment model.

---

# 14. Foreign Keys

The important foreign-key relationships are:

```text
parking_spot.floor_id
        ↓
parking_floors.id
```

```text
parking_tickets.vehicle_id
        ↓
vehicles.id
```

```text
parking_tickets.parking_spot_id
        ↓
parking_spot.id
```

```text
payments.parking_ticket_id
        ↓
parking_tickets.id
```

These relationships maintain referential integrity between the tables.

---

# 15. Unique Constraints

Unique constraints protect business identifiers.

## Vehicle Number

```text
vehicles.vehicle_number
```

prevents duplicate vehicle records.

## Floor Number

```text
parking_floors.floor_number
```

uniquely identifies a floor.

## Spot Number

```text
parking_spot.spot_number
```

prevents two parking spaces from using the same identifier.

## Ticket Number

```text
parking_tickets.ticket_number
```

uniquely identifies a parking session.

## Payment Ticket Reference

```text
payments.parking_ticket_id
```

supports the current one-to-one ticket/payment relationship.

---

# 16. Indexing Strategy

Indexes are created based on actual application query patterns.

## Parking Spot Allocation

The application frequently searches using:

```sql
WHERE spot_type = ?
AND status = ?
```

Therefore the table uses a composite index:

```text
(spot_type, status)
```

This supports finding available spots of a required type.

---

# 17. Active Ticket Lookup

Checkout needs to find an active ticket associated with a vehicle.

The relevant ticket-side access pattern uses:

```text
vehicle_id
+
status
```

Therefore:

```text
(vehicle_id, status)
```

is indexed.

---

# 18. Why Not Index Every Column?

Indexes improve many read operations but have costs.

For every:

```text
INSERT
UPDATE
DELETE
```

the database may also need to maintain related indexes.

Therefore columns such as:

```text
owner_name
total_fee
payment_mode
floor_name
```

are not indexed merely because they exist.

Indexes should support real constraints and query patterns.

---

# 19. Optimistic Locking

`ParkingSpot` contains:

```java
@Version
private Long version;
```

Hibernate uses this column to detect stale updates.

Example:

```text
id       = 10
status   = AVAILABLE
version  = 5
```

When Hibernate updates the entity, it can use the current version as part of the update condition.

After a successful update:

```text
version = 6
```

If another transaction already modified the same version, Hibernate can detect that the entity state is stale.

---

# 20. Pessimistic Locking

Parking allocation also uses:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

for the query that finds an available parking spot.

Conceptually:

```sql
SELECT ...
FROM parking_spot
WHERE spot_type = ?
AND status = ?
ORDER BY id
LIMIT 1
FOR UPDATE;
```

This is important because spot allocation is a concurrency-sensitive operation.

The lock is held within the surrounding transaction.

---

# 21. Transaction Consistency

Check-in involves:

```text
ParkingSpot
+
ParkingTicket
```

Checkout involves:

```text
ParkingTicket
+
Payment
+
ParkingSpot
```

These operations use Spring transaction management so related database changes participate in one logical transaction.

Example checkout:

```text
BEGIN

Ticket ACTIVE → COMPLETED
Set exit time
Calculate fee
Set total fee
Insert payment
Release parking spot

COMMIT
```

If an unchecked failure occurs before completion:

```text
ROLLBACK
```

---

# 22. Fetch Strategy

Relationships are generally configured using:

```java
FetchType.LAZY
```

This prevents Hibernate from automatically retrieving entire object graphs when they are not needed.

For example, loading a vehicle does not automatically require loading its complete parking-ticket history.

Potential N+1 scenarios should be handled for the specific query/use case rather than globally changing relationships to `EAGER`.

---

# 23. Open-In-View

The application uses:

```properties
spring.jpa.open-in-view=false
```

This means persistence access should happen inside the intended service/transaction boundaries.

The REST API returns DTOs instead of directly exposing persistence entities.

---

# 24. Monetary Data

Parking fees and payment amounts use:

```java
BigDecimal
```

with decimal precision/scale rather than floating-point types.

Example:

```java
@Column(
    precision = 10,
    scale = 2
)
private BigDecimal totalFee;
```

This is appropriate for monetary values because binary floating-point types such as `double` can introduce precision errors.

---

# 25. Example Database State During Check-In

Before:

```text
parking_spot

C-101 | COMPACT | AVAILABLE
```

Request:

```text
Vehicle MH27BD3354 checks in
```

After:

```text
vehicles

MH27BD3354 | Amir | CAR
```

```text
parking_spot

C-101 | COMPACT | OCCUPIED
```

```text
parking_tickets

TKT-... | ACTIVE | vehicle=MH27BD3354 | spot=C-101
```

---

# 26. Example Database State During Check-Out

Before:

```text
Ticket
status = ACTIVE

Spot
status = OCCUPIED
```

Checkout occurs.

After successful payment:

```text
Ticket
status     = COMPLETED
exit_time  = populated
total_fee  = calculated
```

```text
Payment
amount          = calculated fee
payment_status  = SUCCESS
```

```text
Spot
status = AVAILABLE
```

---

# 27. Database Persistence with Docker

MySQL runs as a Docker Compose service.

The Spring Boot container connects using:

```text
jdbc:mysql://mysql:3306/smart_parking_db
```

where:

```text
mysql
```

is the Docker Compose service hostname.

Database files are stored in a named Docker volume.

Conceptually:

```text
MySQL Container
      |
      v
/var/lib/mysql
      |
      v
Docker Named Volume
```

Therefore normal container recreation does not remove the database contents.

---

# 28. Summary

The database design focuses on:

- Referential integrity
- Unique business identifiers
- Correct entity relationships
- Transaction consistency
- Efficient parking-spot lookup
- Efficient active-ticket lookup
- Monetary precision
- Concurrent allocation protection
- Historical parking-session preservation
- Persistent Docker storage

The schema is intentionally designed around the actual access patterns and business operations of the Smart Parking Management System.