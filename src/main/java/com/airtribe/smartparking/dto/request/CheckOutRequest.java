package com.airtribe.smartparking.dto.request;

import com.airtribe.smartparking.enums.PaymentMode;
import com.airtribe.smartparking.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckOutRequest {

    @Schema(
            description = "Parking ticket number generated during check-in",
            example = "TKT-1784901267717-9B08F"
    )
    @NotNull(message = "Ticket number is required")
    private String ticketNumber;


    @Schema(
            description = "Vehicle registration number",
            example = "MH27BD3354"
    )
    @NotNull(message = "Vehicle number is required")
    private String vehicleNumber;


    @Schema(
            description = "Payment method used during checkout",
            example = "UPI"
    )
    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;


    @Schema(
            description = "Result/status of the payment",
            example = "SUCCESS"
    )
    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;
}
