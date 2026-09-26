package com.airtribe.smartparking.exception.base;

public class SmartParkingException extends RuntimeException {

    public SmartParkingException(String message) {

        super(message);
    }

    public SmartParkingException(Throwable cause) {

        super(cause);
    }

    public SmartParkingException(String message, Throwable cause) {

        super(message, cause);
    }


}
