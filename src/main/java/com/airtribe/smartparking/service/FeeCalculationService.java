package com.airtribe.smartparking.service;

import com.airtribe.smartparking.entity.ParkingTicket;

import java.math.BigDecimal;

public interface FeeCalculationService {

    BigDecimal calculateParkingFee(ParkingTicket parkingTicket);


}
