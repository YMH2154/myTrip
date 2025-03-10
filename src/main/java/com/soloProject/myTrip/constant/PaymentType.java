package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentType {
    DEPOSIT("예약금"),
    BALANCE("잔금");

    private final String description;
}
