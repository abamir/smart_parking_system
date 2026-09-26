package com.airtribe.smartparking.entity;

import com.airtribe.smartparking.entity.base.BaseEntity;
import com.airtribe.smartparking.enums.PaymentMode;
import com.airtribe.smartparking.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment  extends BaseEntity {

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parking_ticket_id",
            nullable = false,
            unique = true
    )
    private ParkingTicket parkingTicket;
}
