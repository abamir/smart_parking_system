package com.airtribe.smartparking.exception.handler;

import com.airtribe.smartparking.common.response.ErrorResponse;
import com.airtribe.smartparking.exception.*;
import com.airtribe.smartparking.exception.base.SmartParkingException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Logger

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle all SmartParkingException
    @ExceptionHandler(SmartParkingException.class)
    public ResponseEntity<ErrorResponse> handleSmartParkingException(
            SmartParkingException exception,
            HttpServletRequest request) {

        HttpStatus status = resolveHttpStatus(exception);

        return ResponseEntity
                .status(status)
                .body(buildErrorResponse(
                        status,
                        exception.getMessage(),
                        request
                ));
    }


    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {


        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validationErrors.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed.")
                .path(request.getRequestURI())
                .errors(validationErrors)
                .build();

        return ResponseEntity
                .badRequest()
                .body(errorResponse);

    }


    //Generic Exception Handler to handle unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception,
            HttpServletRequest request) {

        LOGGER.error("Unexpected exception occurred.", exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred. Please try again later.",
                        request
                ));
    }

    // Helper method to resolve HTTP status based on exception type
    private HttpStatus resolveHttpStatus(SmartParkingException exception) {

        if (exception instanceof ResourceNotFoundException
                || exception instanceof TicketNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }

        if (exception instanceof ParkingSpotNotAvailableException
                || exception instanceof VehicleAlreadyParkedException) {
            return HttpStatus.CONFLICT;
        }

        if (exception instanceof PaymentFailedException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return HttpStatus.BAD_REQUEST;
    }


    // Helper method to build error response that can be used by all exception handlers
    private ErrorResponse buildErrorResponse
    (HttpStatus status,
     String message,
     HttpServletRequest request
    ) {

        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }


}
