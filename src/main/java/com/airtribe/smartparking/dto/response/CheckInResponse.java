package com.airtribe.smartparking.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckInResponse {

    private String ticketNumber;

    private String spotNumber;

    private Integer floorNumber;

    private LocalDateTime entryTime;

    private String message;
}
