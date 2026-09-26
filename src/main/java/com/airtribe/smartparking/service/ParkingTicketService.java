package com.airtribe.smartparking.service;

import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.entity.Vehicle;

import java.time.LocalDateTime;

public interface ParkingTicketService {

    ParkingTicket createParkingTicket(
            Vehicle vehicle,
            ParkingSpot parkingSpot
    );

    ParkingTicket findByTicketNumber(String ticketNumber);

    ParkingTicket findActiveTicket(String vehicleNumber);

    ParkingTicket closeParkingTicket(
            ParkingTicket parkingTicket,
            LocalDateTime exitTime
    );

    ParkingTicket save(ParkingTicket parkingTicket);
}
