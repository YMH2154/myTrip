package com.soloProject.myTrip.constant;

public enum ThemeCategory {
  HONEYMOON("honeymoon","허니문"),
  GOLF("golf","골프"),
  CRUISE("cruise","크루즈"),
  RESORT("resort","리조트");

  private final String link;
  private final String description;

  ThemeCategory(String link, String description) {
    this.link = link;
    this.description = description;
  }

  public String getLink(){
    return link;
  }
  public String getDescription(){
    return description;
  }

}