package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ThemeCategory {
  HONEYMOON("허니문"),
  GOLF("골프"),
  CRUISE("크루즈"),
  RESORT("리조트");

  private final String description;

  public String getDescription(){
    return description;
  }

}