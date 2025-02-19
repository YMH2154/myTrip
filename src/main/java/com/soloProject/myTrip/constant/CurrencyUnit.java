package com.soloProject.myTrip.constant;

public enum CurrencyUnit {
  KRW("원"),
  USD("달러"),
  EUR("유로"),
  JPY("엔"),
  CHF("스위스 프랑"),
  THB("바트");

  private final String description;

  CurrencyUnit(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}