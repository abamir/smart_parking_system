package com.airtribe.smartparking.repository;

import com.airtribe.smartparking.entity.ParkingFloor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParkingFloorRepository extends JpaRepository<ParkingFloor, Long> {

    boolean existsByFloorNumber(Integer floorNumber);

}
