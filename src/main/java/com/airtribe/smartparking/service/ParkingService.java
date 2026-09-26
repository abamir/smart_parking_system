package com.airtribe.smartparking.service;

import com.airtribe.smartparking.dto.request.CheckInRequest;
import com.airtribe.smartparking.dto.request.CheckOutRequest;
import com.airtribe.smartparking.dto.response.ParkingAvailabilityResponse;
import com.airtribe.smartparking.dto.response.ParkingTicketResponse;
import com.airtribe.smartparking.dto.response.PaymentResponse;

public interface ParkingService {

    ParkingTicketResponse checkIn(
            CheckInRequest request
    );

    PaymentResponse checkOut(
            CheckOutRequest request
    );

    ParkingAvailabilityResponse getParkingAvailability();



}
