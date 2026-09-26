package com.airtribe.smartparking.service.impl;

import com.airtribe.smartparking.dto.request.CheckInRequest;
import com.airtribe.smartparking.dto.request.CheckOutRequest;
import com.airtribe.smartparking.dto.response.ParkingAvailabilityResponse;
import com.airtribe.smartparking.dto.response.ParkingTicketResponse;
import com.airtribe.smartparking.dto.response.PaymentResponse;
import com.airtribe.smartparking.entity.ParkingSpot;
import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.entity.Payment;
import com.airtribe.smartparking.entity.Vehicle;
import com.airtribe.smartparking.enums.PaymentStatus;
import com.airtribe.smartparking.exception.VehicleAlreadyParkedException;
import com.airtribe.smartparking.mapper.GenericMapper;
import com.airtribe.smartparking.mapper.ParkingTicketMapper;
import com.airtribe.smartparking.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {


    private final VehicleService vehicleService;
    private final ParkingSpotService parkingSpotService;
    private final ParkingTicketService parkingTicketService;
    private final FeeCalculationService feeCalculationService;
    private final PaymentService paymentService;
    private final GenericMapper mapper;
    private final ParkingTicketMapper parkingTicketMapper;

    @Override
    @Transactional
    public ParkingTicketResponse checkIn(CheckInRequest request) {

        log.info("Check-in requested for vehicleNumber={}, vehicleType={}", request.getVehicleNumber(), request.getVehicleType());

        // Validate request
        validateCheckInRequest(request);

        //Validate vehicle is already park
        validateVehicleIsAlreadyParked(request.getVehicleNumber());

        Vehicle vehicle = getOrCreateVehicle(request);

        log.debug("Vehicle resolved successfully for vehicleNumber={}",
                vehicle.getVehicleNumber());

        ParkingSpot parkingSpot = parkingSpotService.allocateParkingSpot(request.getVehicleType());

        log.debug("Parking spot allocated: spotNumber={}, vehicleNumber={}",
                parkingSpot.getSpotNumber(), request.getVehicleNumber());

        ParkingTicket parkingTicket = parkingTicketService.createParkingTicket(vehicle, parkingSpot);

        log.info("Vehicle check-in completed: vehicleNumber={}, ticketNumber={}, spotNumber={}",
                vehicle.getVehicleNumber(), parkingTicket.getTicketNumber(), parkingSpot.getSpotNumber());

        return parkingTicketMapper.toResponse(parkingTicket);


    }

    @Override
    @Transactional
    public PaymentResponse checkOut(CheckOutRequest request) {

        log.info(
                "Check-out requested: vehicleNumber={}, ticketNumber={}, paymentMode={}",
                request.getVehicleNumber(),
                request.getTicketNumber(),
                request.getPaymentMode()
        );

        // Validate request
        validateCheckOutRequest(request);



        //Find ACTIVE Ticket
        ParkingTicket parkingTicket = parkingTicketService.findActiveTicket(request.getVehicleNumber());

        log.debug("Active ticket found: ticketNumber={}, vehicleNumber={}", parkingTicket.getTicketNumber(), request.getVehicleNumber());

        //Close Ticket
        parkingTicket = parkingTicketService.closeParkingTicket(parkingTicket, LocalDateTime.now());

        //Calculate Fee
        BigDecimal parkingFee = feeCalculationService.calculateParkingFee(parkingTicket);

        log.debug(
                "Parking fee calculated: ticketNumber={}, amount={}",
                parkingTicket.getTicketNumber(),
                parkingFee
        );

        //Update Ticket Fee
        parkingTicket.setTotalFee(parkingFee);
        parkingTicketService.save(parkingTicket);

        //Record Payment
        Payment payment = paymentService.recordPayment(parkingTicket, parkingFee, request.getPaymentMode(), request.getPaymentStatus());

        // Release Spot only if payment is completed
        if (request.getPaymentStatus() == PaymentStatus.SUCCESS) {
            parkingSpotService.releaseParkingSpot(parkingTicket.getParkingSpot().getId());

            log.debug(
                    "Parking spot released: spotNumber={}",
                    parkingTicket.getParkingSpot().getSpotNumber()
            );
        }else {

            log.warn(
                    "Parking payment unsuccessful: ticketNumber={}, paymentStatus={}",
                    parkingTicket.getTicketNumber(),
                    request.getPaymentStatus()
            );
        }

        log.info(
                "Check-out completed: vehicleNumber={}, ticketNumber={}, amount={}, paymentStatus={}",
                request.getVehicleNumber(),
                parkingTicket.getTicketNumber(),
                parkingFee,
                request.getPaymentStatus()
        );


        // Return PaymentResponse
        return mapper.map(payment, PaymentResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ParkingAvailabilityResponse getParkingAvailability() {

        return parkingSpotService.getParkingAvailability();
    }

    // =======================================================
    // Private Helper Methods
    // =======================================================

    private void validateCheckInRequest(CheckInRequest request) {
        if (request.getVehicleNumber() == null || request.getVehicleNumber().isEmpty()) {
            throw new IllegalArgumentException("Vehicle number is required");
        }
        if (request.getVehicleType() == null) {
            throw new IllegalArgumentException("Vehicle type is required");
        }
    }

    private void validateCheckOutRequest(CheckOutRequest request) {
        if (request.getVehicleNumber() == null || request.getVehicleNumber().isEmpty()) {
            throw new IllegalArgumentException("Vehicle number is required");
        }
        if (request.getPaymentMode() == null) {
            throw new IllegalArgumentException("Payment mode is required");
        }
        if (request.getPaymentStatus() == null) {
            throw new IllegalArgumentException("Payment status is required");
        }
    }

    private void validateVehicleIsAlreadyParked(String vehicleNumber) {
        if (vehicleService.isVehicleAlreadyParked(vehicleNumber)) {
            throw new VehicleAlreadyParkedException("Vehicle is already parked");
        }
    }

    private Vehicle getOrCreateVehicle(CheckInRequest request) {

        return vehicleService.findVehicle(request.getVehicleNumber()).orElseGet(() -> {

            Vehicle vehicle = mapper.map(request, Vehicle.class);

            return vehicleService.saveVehicle(vehicle);
        });
    }

}
