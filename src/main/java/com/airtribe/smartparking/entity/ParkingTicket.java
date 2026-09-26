package com.airtribe.smartparking.entity;

import com.airtribe.smartparking.entity.base.BaseEntity;
import com.airtribe.smartparking.enums.ParkingTicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "parking_tickets",
        indexes = {
                @Index(
                        name = "idx_ticket_vehicle_status",
                        columnList = "vehicle_id,status"
                )
        }
)
public class ParkingTicket extends BaseEntity {

    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "total_fee", precision = 10, scale = 2)
    private BigDecimal totalFee;

    @Enumerated(EnumType.STRING)
    private ParkingTicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_spot_id")
    private ParkingSpot parkingSpot;

    @OneToOne(mappedBy = "parkingTicket", fetch = FetchType.LAZY)
    private Payment payment;

}
