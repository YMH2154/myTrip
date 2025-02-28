package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CouponDuration {
    WEEK("일주일"),
    MONTH("한 달"),
    THREE("3개월"),
    SIX("6개월"),
    YEAR("1년");

    private final String description;
}
