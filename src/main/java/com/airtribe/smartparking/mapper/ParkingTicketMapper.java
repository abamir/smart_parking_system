package com.airtribe.smartparking.mapper;

import com.airtribe.smartparking.dto.response.ParkingTicketResponse;
import com.airtribe.smartparking.entity.ParkingTicket;
import org.springframework.stereotype.Component;

@Component
public class ParkingTicketMapper {

    public ParkingTicketResponse toResponse(ParkingTicket ticket) {

        if (ticket == null) {
            return null;
        }

        return ParkingTicketResponse.builder()
                .ticketNumber(ticket.getTicketNumber())
                .vehicleNumber(ticket.getVehicle().getVehicleNumber())
                .vehicleType(ticket.getVehicle().getVehicleType())
                .spotNumber(ticket.getParkingSpot().getSpotNumber())
                .floorNumber(ticket.getParkingSpot().getParkingFloor().getFloorNumber())
                .entryTime(ticket.getEntryTime())
                .exitTime(ticket.getExitTime())
                .totalFee(ticket.getTotalFee())
                .status(ticket.getStatus())
                .build();
    }
}
