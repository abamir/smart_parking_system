package com.airtribe.smartparking.service.impl;

import com.airtribe.smartparking.dto.request.VehicleRequest;
import com.airtribe.smartparking.dto.response.VehicleResponse;
import com.airtribe.smartparking.entity.Vehicle;
import com.airtribe.smartparking.enums.ParkingTicketStatus;
import com.airtribe.smartparking.exception.ResourceNotFoundException;
import com.airtribe.smartparking.mapper.GenericMapper;
import com.airtribe.smartparking.repository.ParkingTicketRepository;
import com.airtribe.smartparking.repository.VehicleRepository;
import com.airtribe.smartparking.service.VehicleService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ParkingTicketRepository parkingTicketRepository;
    private final GenericMapper mapper;



    @Override
    public VehicleResponse registerVehicle(VehicleRequest request) {

        Vehicle vehicle = mapper.map(request, Vehicle.class);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return mapper.map(savedVehicle, VehicleResponse.class);
    }

    @Override
    public Vehicle saveVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle findByVehicleNumber(String vehicleNumber) {

        return vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with number " + vehicleNumber));
    }

    @Override
    public boolean isVehicleAlreadyParked(String vehicleNumber) {
        return parkingTicketRepository.
                existsByVehicleVehicleNumberAndStatus(vehicleNumber, ParkingTicketStatus.ACTIVE);
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> findVehicle(String vehicleNumber) {
        return vehicleRepository.findByVehicleNumber(vehicleNumber);
    }
}
