package com.airtribe.smartparking.common.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private String error;

    private String message;

    private int status;

    private LocalDateTime timestamp;

    private String path;
    private Map<String, String> errors;
}
