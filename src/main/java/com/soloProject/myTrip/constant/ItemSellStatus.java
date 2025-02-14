package com.soloProject.myTrip.constant;

import lombok.Getter;

@Getter
public enum ItemSellStatus {
    SELL("모집중"),
    CONFIRMED("출발 확정"),
    SOLDOUT("마감");

    private final String description;

    ItemSellStatus(String description){
        this.description = description;
    }

}
