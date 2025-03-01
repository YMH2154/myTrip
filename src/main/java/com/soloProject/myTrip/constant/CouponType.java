package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CouponType {
    AMOUNT("정액"),
    PERCENTAGE("정률");

    private final String description;
}
