package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ReservationStatus {
    RESERVED("결제 대기"),
    PAID("결제 완료"),
    CANCEL("예약 취소");

    private final String description;


}
