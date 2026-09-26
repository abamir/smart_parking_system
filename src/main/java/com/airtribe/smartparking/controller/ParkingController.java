package com.airtribe.smartparking.controller;

import com.airtribe.smartparking.common.constants.ApiConstants;
import com.airtribe.smartparking.common.response.ApiResponseDto;
import com.airtribe.smartparking.common.util.ResponseBuilder;
import com.airtribe.smartparking.dto.request.CheckInRequest;
import com.airtribe.smartparking.dto.request.CheckOutRequest;
import com.airtribe.smartparking.dto.response.ParkingAvailabilityResponse;
import com.airtribe.smartparking.dto.response.ParkingTicketResponse;
import com.airtribe.smartparking.dto.response.PaymentResponse;
import com.airtribe.smartparking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.API_V1 + ApiConstants.PARKING)
@Tag(name = "Parking Management",
        description = "REST APIs for Smart Parking System")
public class ParkingController {

    private final ParkingService parkingService;


    @PostMapping(ApiConstants.CHECK_IN)
    @Operation(summary = "Vehicle Check-In",
            description = "Registers a vehicle, allocates an available parking spot, and generates a parking ticket."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vehicle checked in successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Vehicle already parked")
    })
    public ResponseEntity<ApiResponseDto<ParkingTicketResponse>>
    checkInVehicle(@Valid @RequestBody CheckInRequest request) {

        ParkingTicketResponse response = parkingService.checkIn(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseBuilder.success("Vehicle checked in successfully", response));


    }

    @PostMapping(ApiConstants.CHECK_OUT)
    @Operation(summary = "Vehicle Check-Out",
            description = "Checks out a vehicle, calculates the parking fee, records the payment, and releases the parking spot."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle checked out successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Active parking ticket not found")
    })
    public ResponseEntity<ApiResponseDto<PaymentResponse>> checkOutVehicle(
            @Valid
            @RequestBody
            CheckOutRequest checkOutRequest) {

        PaymentResponse response = parkingService.checkOut(checkOutRequest);

        return ResponseEntity.ok()
                .body(ResponseBuilder.success("Vehicle checked out successfully", response));
    }

    @GetMapping(ApiConstants.AVAILABILITY)
    @Operation(
            summary = "Parking Availability",
            description = "Returns the current availability of parking spots."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Parking availability retrieved successfully"
    )
    public ResponseEntity<ApiResponseDto<ParkingAvailabilityResponse>> getParkingAvailability() {

        ParkingAvailabilityResponse response =
                parkingService.getParkingAvailability();

        return ResponseEntity.ok(
                ResponseBuilder.success(
                        "Parking availability fetched successfully.",
                        response
                )
        );
    }

}
