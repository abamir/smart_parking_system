package com.airtribe.smartparking.dto.response;

import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.entity.Vehicle;
import com.airtribe.smartparking.enums.ParkingTicketStatus;
import com.airtribe.smartparking.enums.VehicleType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingTicketResponse {

    private String ticketNumber;

    private String vehicleNumber;

    private VehicleType vehicleType;

    private String spotNumber;

    private Integer floorNumber;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private BigDecimal totalFee;

    private ParkingTicketStatus status;
}
