package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IamportPayment {
  private String imp_uid;
  private String merchant_uid;
  private Integer amount;
  private String status;
  private String pay_method;
  private String pg_provider;
  private String card_name;
  private String card_number;
  private Integer card_quota;
  private String name;
  private String buyer_name;
  private String buyer_email;
  private String buyer_tel;
  private String paid_at;
  private String failed_at;
  private String cancel_amount;
  private String cancel_reason;
  private String receipt_url;
}