package com.soloProject.myTrip.constant;

public enum TravelType {
  DOMESTIC("국내여행"),
  OVERSEAS("해외여행"),
  THEME("테마여행");

  private final String description;

  TravelType(String description) {
    this.description = description;
  }

  public String getDescription(){
    return description;
  }
}