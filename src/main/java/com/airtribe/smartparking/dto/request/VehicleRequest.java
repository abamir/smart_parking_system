package com.airtribe.smartparking.dto.request;

import com.airtribe.smartparking.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleRequest {

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 20)
    private String vehicleNumber;

    @NotBlank(message = "Owner name is required")
    @Size(max = 100)
    private String ownerName;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

}
