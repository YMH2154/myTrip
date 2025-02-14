package com.soloProject.myTrip.constant;


public enum DomesticCategory {
  SEOUL("서울"),
  BUSAN("부산"),
  JEJU("제주도"),
  GANGWON("강원도"),
  GYEONGJU("경주"),
  JEONJU("전주"),
  YEOSU("여수");

  private final String description;

  DomesticCategory(String description) {
    this.description = description;
  }

  public String getDescription(){
    return description;
  }
}