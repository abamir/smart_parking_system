package com.airtribe.smartparking.entity;

import com.airtribe.smartparking.entity.base.BaseEntity;
import com.airtribe.smartparking.enums.FloorStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "parking_floors",

        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_floor_number",
                        columnNames = "floor_number"
                )
        }
)
public class ParkingFloor extends BaseEntity {

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "floor_name", nullable = false, length = 50)
    private String floorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FloorStatus status;

    @OneToMany(
            mappedBy = "parkingFloor",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ParkingSpot> parkingSpots = new ArrayList<>();


}
