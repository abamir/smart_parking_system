package com.airtribe.smartparking.dto.request;

import com.airtribe.smartparking.enums.PaymentMode;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull
    private PaymentMode paymentMode;

}
