package com.airtribe.smartparking.service.impl;

import com.airtribe.smartparking.entity.ParkingTicket;
import com.airtribe.smartparking.entity.Payment;
import com.airtribe.smartparking.enums.PaymentMode;
import com.airtribe.smartparking.enums.PaymentStatus;
import com.airtribe.smartparking.exception.ResourceNotFoundException;
import com.airtribe.smartparking.repository.PaymentRepository;
import com.airtribe.smartparking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public Payment recordPayment(ParkingTicket parkingTicket,
                                 BigDecimal amount,
                                 PaymentMode paymentMode,
                                 PaymentStatus paymentStatus) {
        Payment payment = Payment.builder()
                .amount(amount)
                .paymentMode(paymentMode)
                .paymentStatus(paymentStatus)
                .paidAt(LocalDateTime.now())
                .parkingTicket(parkingTicket)
                .build();

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment findByParkingTicketId(Long parkingTicketId) {
        return paymentRepository.findByParkingTicketId(parkingTicketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found for ticket : " + parkingTicketId));
    }
}
