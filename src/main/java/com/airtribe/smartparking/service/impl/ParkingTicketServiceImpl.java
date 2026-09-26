package com.airtribe.smartparking.service.impl;

import com.airtribe.smartparking.common.util.TicketNumberGenerator;
import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.entity.Vehicle;
import com.airtribe.smartparking.enums.ParkingTicketStatus;
import com.airtribe.smartparking.exception.ResourceNotFoundException;
import com.airtribe.smartparking.repository.ParkingTicketRepository;
import com.airtribe.smartparking.service.ParkingTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ParkingTicketServiceImpl implements ParkingTicketService {

    private final ParkingTicketRepository parkingTicketRepository;


    @Override
    public ParkingTicket createParkingTicket(Vehicle vehicle, ParkingSpot parkingSpot) {

        ParkingTicket ticket = ParkingTicket.builder()
                .ticketNumber(TicketNumberGenerator.generate())
                .entryTime(LocalDateTime.now())
                .vehicle(vehicle)
                .status(ParkingTicketStatus.ACTIVE)
                .parkingSpot(parkingSpot)
                .build();


        return parkingTicketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingTicket findByTicketNumber(String ticketNumber) {
        return parkingTicketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found with number " + ticketNumber));
    }

    @Override
    public ParkingTicket findActiveTicket(String vehicleNumber) {

        return parkingTicketRepository.findByVehicleVehicleNumberAndStatus(vehicleNumber, ParkingTicketStatus.ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Active ticket not found for vehicle number " + vehicleNumber));
    }

    @Override
    public ParkingTicket closeParkingTicket(ParkingTicket parkingTicket, LocalDateTime exitTime) {

        parkingTicket.setExitTime(exitTime);
        parkingTicket.setStatus(ParkingTicketStatus.COMPLETED);

        return parkingTicketRepository.save(parkingTicket);


    }

    @Override
    public ParkingTicket save(ParkingTicket parkingTicket) {
        return parkingTicketRepository.save(parkingTicket);
    }
}
