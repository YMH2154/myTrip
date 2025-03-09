package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DomesticCategory {
  SEOUL("서울"),
  BUSAN("부산"),
  JEJU("제주도");

  private final String description;

  public String getDescription(){
    return description;
  }
}