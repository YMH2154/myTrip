package com.soloProject.myTrip.constant;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum OverseasCategory {
  // 유럽
  FRANCE("프랑스", "유럽"),
  ITALY("이탈리아", "유럽"),
  SPAIN("스페인", "유럽"),
  UK("영국", "유럽"),

  // 아시아
  JAPAN("일본", "아시아"),
  CHINA("중국", "아시아"),
  TAIWAN("대만", "아시아"),
  VIETNAM("베트남", "아시아"),
  THAILAND("태국", "아시아"),

  // 미주
  USA("미국", "미주"),
  CANADA("캐나다", "미주"),
  HAWAII("하와이", "미주"),
  GUAM("괌", "미주");

  private final String description;
  private final String region;

  OverseasCategory(String description, String region) {
    this.description = description;
    this.region = region;
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