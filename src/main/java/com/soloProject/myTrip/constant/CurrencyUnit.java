package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CurrencyUnit {
  KRW("원" , "₩"),
  USD("달러", "$"),
  EUR("유로", "€"),
  JPY("엔", "¥"),
  CNY("위안", "元"),
  CHF("스위스 프랑", "sFr"),
  THB("바트", "฿");

  private final String description;
  private final String sign;

  public String getDescription() {
    return description;
  }
  public String getSign(){
    return sign;
  }
}