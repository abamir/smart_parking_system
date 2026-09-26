package com.airtribe.smartparking.common.util;

import com.airtribe.smartparking.common.response.ApiResponseDto;

import java.time.LocalDateTime;

public class ResponseBuilder {
    private ResponseBuilder() {
    }

    public static <T> ApiResponseDto<T> success(String message, T data) {

        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
