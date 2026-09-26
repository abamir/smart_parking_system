package com.airtribe.smartparking.exception;

import com.airtribe.smartparking.exception.base.SmartParkingException;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class PaymentFailedException extends SmartParkingException {

    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(Throwable cause) {
        super(cause);
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
