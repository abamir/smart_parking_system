package com.airtribe.smartparking.dto.response;

import com.airtribe.smartparking.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckOutResponse {

    private String ticketNumber;

    private LocalDateTime exitTime;

    private BigDecimal parkingFee;

    private PaymentStatus paymentStatus;

    private String message;

}
