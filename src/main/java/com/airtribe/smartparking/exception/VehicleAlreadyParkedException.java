package com.airtribe.smartparking.exception;

import com.airtribe.smartparking.exception.base.SmartParkingException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.CONFLICT;

@ResponseStatus(CONFLICT)
public class VehicleAlreadyParkedException extends SmartParkingException {

    public VehicleAlreadyParkedException(String message) {
        super(message);
    }

    public VehicleAlreadyParkedException(Throwable cause) {
        super(cause);
    }

    public VehicleAlreadyParkedException(String message, Throwable cause) {
        super(message, cause);
    }
}
