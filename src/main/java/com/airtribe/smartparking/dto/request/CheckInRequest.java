package com.airtribe.smartparking.dto.request;

import com.airtribe.smartparking.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckInRequest {

    @Schema(
            description = "Vehicle registration number",
            example = "MH27BD3354"
    )
    @NotBlank(message = "Vehicle number is required")
    @Size(max = 20)
    private String vehicleNumber;


    @Schema(
            description = "Name of the vehicle owner",
            example = "Amir"
    )
    @Size(max = 100)
    private String ownerName;


    @Schema(
            description = "Type of vehicle",
            example = "CAR"
    )
    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

}
