package com.soloProject.myTrip.constant;

public enum ThemeCategory {
  HONEYMOON("허니문"),
  GOLF("골프"),
  CRUISE("크루즈"),
  RESORT("리조트");

  private final String description;

  ThemeCategory(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}