package com.airtribe.smartparking.exception;

import com.airtribe.smartparking.exception.base.SmartParkingException;

public class ParkingSpotNotAvailableException extends SmartParkingException {

    public ParkingSpotNotAvailableException(String message) {
        super(message);
    }
}
