package com.airtribe.smartparking.service;

import com.airtribe.smartparking.dto.response.ParkingAvailabilityResponse;
import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.enums.VehicleType;

public interface ParkingSpotService {

    ParkingSpot allocateParkingSpot(VehicleType vehicleType);

    void releaseParkingSpot(Long parkingSpotId);

    ParkingSpot findById(Long parkingSpotId);

    ParkingAvailabilityResponse getParkingAvailability();

}
