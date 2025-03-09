package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum OverseasCategory {
  // 유럽
  EAST("동유럽", "유럽"),
  WEST("서유럽", "유럽"),
  NORTH( "북유럽", "유럽"),
  SOUTH("남유럽","유럽"),


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

    public static Set<String> getUniqueRegions() {
    return Arrays.stream(OverseasCategory.values())
        .map(OverseasCategory::getRegion)
        .collect(Collectors.toSet());
  }
}