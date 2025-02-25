package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DomesticCategory {
  SEOUL("seoul","서울"),
  BUSAN("busan","부산"),
  JEJU("jeju","제주도");

  private final String link;
  private final String description;

  public String getDescription(){
    return description;
  }

  public String getLink(){
    return link;
  }
}