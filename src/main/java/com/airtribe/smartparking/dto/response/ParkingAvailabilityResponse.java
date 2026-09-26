package com.airtribe.smartparking.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingAvailabilityResponse {

    private int bikeAvailable;

    private int compactAvailable;

    private int largeAvailable;

    private int busAvailable;
}
