package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.PaymentMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentRequestDto {
  private String reservationNumber;
  private Integer amount;
  private PaymentMethod paymentMethod;
  private String itemName;
  private String buyerName;
  private String buyerEmail;
  private String buyerTel;

  @Builder
  public PaymentRequestDto(String reservationNumber, Integer amount, PaymentMethod paymentMethod,
      String itemName, String buyerName, String buyerEmail, String buyerTel) {
    this.reservationNumber = reservationNumber;
    this.amount = amount;
    this.paymentMethod = paymentMethod;
    this.itemName = itemName;
    this.buyerName = buyerName;
    this.buyerEmail = buyerEmail;
    this.buyerTel = buyerTel;
  }
}