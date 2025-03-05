package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum QuestionType {
    ITEM("상품 관련"),
    PAYMENT("결제 관련"),
    RESERVATION("예약 관련"),
    MEMBER("개인 정보 관련"),
    ETC("기타");

    private final String description;
}
