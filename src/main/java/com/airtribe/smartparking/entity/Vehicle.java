package com.airtribe.smartparking.entity;

import com.airtribe.smartparking.entity.base.BaseEntity;
import com.airtribe.smartparking.enums.VehicleType;
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
@Table(name = "vehicles",

       /* indexes = {
                @Index(name = "idx_vehicle_number", columnList = "vehicle_number")
        },*/

        uniqueConstraints = {@UniqueConstraint(columnNames = {"vehicle_number"})}
)
public class Vehicle extends BaseEntity {

    @Column(name = "vehicle_number", nullable = false, length = 20)
    private String vehicleNumber;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @OneToMany(
            mappedBy = "vehicle",
            fetch = FetchType.LAZY
    )
    @Builder.Default

    private List<ParkingTicket> parkingTickets = new ArrayList<>();

}
