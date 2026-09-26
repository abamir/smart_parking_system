package com.airtribe.smartparking.dto.response;

import com.airtribe.smartparking.enums.ParkingSpotStatus;
import com.airtribe.smartparking.enums.ParkingSpotType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParkingSpotResponse {

    private String spotNumber;

    private ParkingSpotType spotType;

    private ParkingSpotStatus status;

    private Integer floorNumber;


}
