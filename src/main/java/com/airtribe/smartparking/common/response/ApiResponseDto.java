package com.airtribe.smartparking.common.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDto<T> {

    private boolean success;

    private String message;

    private T data;

    private LocalDateTime timestamp;
}
