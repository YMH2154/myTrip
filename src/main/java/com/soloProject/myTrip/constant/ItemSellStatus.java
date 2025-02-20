package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemSellStatus {
    SELL("예약 가능"),
    WAITING("예약 대기"),
    SOLDOUT("예약 마감");

    private final String description;

}
