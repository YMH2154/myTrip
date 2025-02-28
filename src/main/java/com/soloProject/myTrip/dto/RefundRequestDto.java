package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundRequestDto {
    private String reservationNumber;
    private BigDecimal amount;
    private String reason;
} 