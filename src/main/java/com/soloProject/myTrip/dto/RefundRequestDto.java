package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequestDto {
    private String reservationNumber;
    private String amount;
    private String reason;
} 