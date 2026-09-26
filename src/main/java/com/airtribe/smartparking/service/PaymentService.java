package com.airtribe.smartparking.service;

import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.entity.Payment;
import com.airtribe.smartparking.enums.PaymentMode;
import com.airtribe.smartparking.enums.PaymentStatus;

import java.math.BigDecimal;

public interface PaymentService {

    Payment recordPayment(
            ParkingTicket parkingTicket,
            BigDecimal amount,
            PaymentMode paymentMode,
            PaymentStatus paymentStatus
    );

    Payment findByParkingTicketId(Long parkingTicketId);
}
