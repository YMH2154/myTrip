package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardPaymentVerifyRequest {
  private String impUid;
  private String merchantUid;
  private String reservationNumber;
  private String amount;
  private Long couponId;
  private Integer usedMileage;
}