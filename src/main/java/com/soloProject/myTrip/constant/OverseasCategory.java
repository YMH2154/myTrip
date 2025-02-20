package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum OverseasCategory {
  // 유럽
  FRANCE("france","프랑스", "유럽"),
  ITALY("italy","이탈리아", "유럽"),
  SPAIN("spain","스페인", "유럽"),
  UK("uk","영국", "유럽"),
  SWISS("swiss","스위스","유럽"),
  TURKEY("turkey", "튀르키예", "유럽"),
  EAST("east","동유럽", "유럽"),
  WEST("west", "서유럽", "유럽"),
  EUROPE("europe", "다국가", "유럽"),

  // 아시아
  JAPAN("japan","일본", "아시아"),
  CHINA("china","중국", "아시아"),
  TAIWAN("taiwan","대만", "아시아"),
  VIETNAM("vietnam","베트남", "아시아"),
  THAILAND("thailand","태국", "아시아"),

  // 미주
  USA("usa","미국", "미주"),
  CANADA("canada","캐나다", "미주"),
  HAWAII("hawaii","하와이", "미주"),
  GUAM("guam","괌", "미주");

  private final String link;
  private final String description;
  private final String region;

  public String getLink(){
    return link;
  }

  public String getDescription() {
    return description;
  }

  public String getRegion() {
    return region;
  }

  public static Set<String> getUniqueRegions() {
    return Arrays.stream(OverseasCategory.values())
        .map(OverseasCategory::getRegion)
        .collect(Collectors.toSet());
  }
}