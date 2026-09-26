package com.airtribe.smartparking.dto.response;

import com.airtribe.smartparking.enums.PaymentMode;
import com.airtribe.smartparking.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponse {

    private String ticketNumber;

    private BigDecimal amount;

    private PaymentMode paymentMode;

    private PaymentStatus paymentStatus;

    private LocalDateTime paidAt;
}
