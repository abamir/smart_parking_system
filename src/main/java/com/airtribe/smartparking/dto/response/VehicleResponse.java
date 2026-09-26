package com.airtribe.smartparking.dto.response;

import com.airtribe.smartparking.enums.VehicleType;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleResponse {

    private String vehicleNumber;

    private String ownerName;

    private VehicleType vehicleType;
}
