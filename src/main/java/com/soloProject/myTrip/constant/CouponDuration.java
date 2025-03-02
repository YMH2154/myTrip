package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CouponDuration {
    WEEK("일주일", "1w"),
    MONTH("한 달", "1m"),
    THREE("3개월", "3m"),
    SIX("6개월", "6m"),
    YEAR("1년", "1y");

    private final String description;
    private final String search;

    public String getDescription(){
        return this.description;
    }
    public String getSearch(){
        return this.search;
    }
}
