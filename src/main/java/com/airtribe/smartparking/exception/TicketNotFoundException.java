package com.airtribe.smartparking.exception;

import com.airtribe.smartparking.exception.base.SmartParkingException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ResponseStatus(NOT_FOUND)
public class TicketNotFoundException extends SmartParkingException {

    public TicketNotFoundException(String message) {
        super(message);
    }

    public TicketNotFoundException(Throwable cause) {
        super(cause);
    }

    public TicketNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
