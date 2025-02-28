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
  EAST("east","동유럽", "유럽"),
  WEST("west", "서유럽", "유럽"),
  NORTH("north", "북유럽", "유럽"),
  SOUTH("south","남유럽","유럽"),


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

    public static Set<String> getUniqueRegions() {
    return Arrays.stream(OverseasCategory.values())
        .map(OverseasCategory::getRegion)
        .collect(Collectors.toSet());
  }
}