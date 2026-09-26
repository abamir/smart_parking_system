package com.airtribe.smartparking.repository;

import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.enums.ParkingTicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingTicketRepository extends JpaRepository<ParkingTicket, Long> {

    Optional<ParkingTicket> findByTicketNumber(String ticketNumber);


    List<ParkingTicket> findByVehicleVehicleNumber(String vehicleNumber);

    Optional<ParkingTicket> findByVehicleVehicleNumberAndStatus(
            String vehicleNumber,
            ParkingTicketStatus status
    );

    boolean existsByVehicleVehicleNumberAndStatus(
            String vehicleNumber,
            ParkingTicketStatus parkingTicketStatus
    );
}
