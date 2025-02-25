package com.soloProject.myTrip.constant;

public enum PaymentMethod {
    KAKAO("카카오페이");

    private final String description;

    PaymentMethod(String description){
        this.description = description;
    }

    public String getDescription(){
        return description;
    }
}
