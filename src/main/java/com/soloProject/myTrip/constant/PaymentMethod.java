package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PaymentMethod {
    KAKAO("카카오 페이"),
    NAVER("네이버 페이"),
    TOSS("토스");

    private final String description;
}
