package com.soloProject.myTrip.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardPaymentPrepareResponse {
  private String merchantUid;
  private String itemName;
  private String buyerEmail;
  private String buyerName;
  private String buyerTel;
}