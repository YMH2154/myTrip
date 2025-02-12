package com.soloProject.myTrip.constant;

public enum ItemSellStatus {
    SELL("모집중"),
    CONFIRMED("출발 확정"),
    SOLDOUT("마감");

    private final String description;

    ItemSellStatus(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
