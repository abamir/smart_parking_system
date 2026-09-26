package com.airtribe.smartparking.service;

import com.airtribe.smartparking.dto.request.VehicleRequest;
import com.airtribe.smartparking.dto.response.VehicleResponse;
import com.airtribe.smartparking.entity.Vehicle;

import java.util.Optional;

public interface VehicleService {

    
    VehicleResponse registerVehicle(VehicleRequest request);

    Vehicle saveVehicle(Vehicle vehicle);

    Vehicle findByVehicleNumber(String vehicleNumber);

    boolean isVehicleAlreadyParked(String vehicleNumber);

    Optional<Vehicle> findVehicle(String vehicleNumber);

}
