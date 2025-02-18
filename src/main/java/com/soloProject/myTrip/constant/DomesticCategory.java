package com.soloProject.myTrip.constant;


public enum DomesticCategory {
  SEOUL("seoul","서울"),
  BUSAN("busan","부산"),
  JEJU("jeju","제주도"),
  GANGWON("gangwon","강원도"),
  GYEONGJU("gyeongju","경주"),
  JEONJU("jeonju","전주"),
  YEOSU("yeosu","여수");

  private final String link;
  private final String description;

  DomesticCategory(String link, String description) {
    this.link = link;
    this.description = description;
  }

  public String getDescription(){
    return description;
  }

  public String getLink(){
    return link;
  }
}