package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CouponType {
    AMOUNT("고정 금액"),
    PERCENTAGE("퍼센트");

    private final String description;
}
