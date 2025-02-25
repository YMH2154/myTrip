package com.soloProject.myTrip.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
  private String reservationNumber;
  private String amount;
}