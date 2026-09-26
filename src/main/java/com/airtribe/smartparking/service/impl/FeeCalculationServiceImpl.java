package com.airtribe.smartparking.service.impl;

import com.airtribe.smartparking.common.constants.ParkingRates;
import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.enums.VehicleType;
import com.airtribe.smartparking.service.FeeCalculationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class FeeCalculationServiceImpl implements FeeCalculationService {

    @Override
    public BigDecimal calculateParkingFee(ParkingTicket parkingTicket) {


        validateParkingTicket(parkingTicket);

        long parkingHours = calculateParkingHours(
                parkingTicket.getEntryTime(),
                parkingTicket.getExitTime()
        );


        BigDecimal hourlyRate = getHourlyRate(
                parkingTicket.getVehicle().getVehicleType()
        );


        return calculateAmount(hourlyRate, parkingHours);


    }

    /**
     * Calculates final parking amount.
     */
    private BigDecimal calculateAmount(BigDecimal hourlyRate,
                                       long parkingHours) {

        return hourlyRate.multiply(BigDecimal.valueOf(parkingHours));
    }

    private BigDecimal getHourlyRate(VehicleType vehicleType) {

        return switch (vehicleType) {

            case BIKE -> ParkingRates.MOTORCYCLE_RATE;

            case CAR -> ParkingRates.CAR_RATE;

            case BUS -> ParkingRates.BUS_RATE;

            case SUV -> ParkingRates.SUV_RATE;

            case TRUCK -> ParkingRates.TRUCK_RATE;
        };
    }

    /**
     * Calculates parking duration in billable hours.
     * Any fraction of an hour is rounded up.
     */
    private long calculateParkingHours(LocalDateTime entryTime, LocalDateTime exitTime) {


        long minutes = Duration
                .between(entryTime, exitTime)
                .toMinutes();

        return Math.max(1, (long) Math.ceil(minutes / 60.0));

    }

    /**
     * Validates the parking ticket before fee calculation.
     */
    private void validateParkingTicket(ParkingTicket parkingTicket) {

        if (parkingTicket == null) {
            throw new IllegalArgumentException("Parking ticket cannot be null");

        }

        if (parkingTicket.getVehicle() == null) {
            throw new IllegalArgumentException("Vehicle details are missing.");
        }

        if (parkingTicket.getEntryTime() == null) {
            throw new IllegalArgumentException("Entry time cannot be null.");
        }

        if (parkingTicket.getExitTime() == null) {
            throw new IllegalArgumentException("Exit time cannot be null.");
        }

        if (parkingTicket.getExitTime().isBefore(parkingTicket.getEntryTime())) {
            throw new IllegalArgumentException("Exit time cannot be before entry time.");
        }
    }


}
