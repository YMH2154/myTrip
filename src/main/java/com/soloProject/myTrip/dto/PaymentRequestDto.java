package com.soloProject.myTrip.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
  private String reservationNumber;
  private BigDecimal amount;
  private Long couponId;
  private Integer usedMileage;
}