package com.airtribe.smartparking.repository;

import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.enums.ParkingSpotStatus;
import com.airtribe.smartparking.enums.ParkingSpotType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    //findByStatus
    List<ParkingSpot> findByStatus(ParkingSpotStatus status);

    //findBySpotTypeAndStatus
    List<ParkingSpot> findBySpotTypeAndStatus(ParkingSpotType spotType, ParkingSpotStatus status);

    //findBySpotNumber
    Optional<ParkingSpot> findBySpotNumber(String spotNumber);


    //findFirstAvailableSpotForUpdate --> Method 1
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ps
            FROM ParkingSpot ps
            WHERE ps.spotType = :spotType
              AND ps.status = :status
            ORDER BY ps.id ASC
            LIMIT 1
            """)
    Optional<ParkingSpot> findFirstAvailableSpotForUpdate(@Param("spotType") ParkingSpotType spotType, @Param("status") ParkingSpotStatus status);


    //findFirstBySpotTypeAndStatusOrderByIdAsc --> Method 2 Recommended
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ParkingSpot> findFirstBySpotTypeAndStatusOrderByIdAsc(
            ParkingSpotType spotType,
            ParkingSpotStatus status
    );

    boolean existsBySpotNumber(String spotNumber);

}
