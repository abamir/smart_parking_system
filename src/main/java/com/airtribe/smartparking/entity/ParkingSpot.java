package com.airtribe.smartparking.entity;

import com.airtribe.smartparking.entity.base.BaseEntity;
import com.airtribe.smartparking.enums.ParkingSpotStatus;
import com.airtribe.smartparking.enums.ParkingSpotType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "parking_spot",

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_spot_number",
                        columnNames = "spot_number"
                )
        },

        indexes = {
                @Index(
                        name = "idx_spot_type_status",
                        columnList = "spot_type,status"
                )
        }
)
public class ParkingSpot extends BaseEntity {

    @Column(name = "spot_number", nullable = false)
    private String spotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "spot_type", nullable = false)
    private ParkingSpotType spotType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingSpotStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private ParkingFloor parkingFloor;

    @Version
    private Long version;
}
