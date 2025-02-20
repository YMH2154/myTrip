package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TravelType {
  DOMESTIC("국내여행"),
  OVERSEAS("해외여행"),
  THEME("테마여행");

  private final String description;

  public String getDescription(){
    return description;
  }
}