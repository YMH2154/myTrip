package com.soloProject.myTrip.constant;

public enum ReservationStatus {
    RESERVED("예약 완료"),
    DEPOSIT_PAID("예약금 결제완료"),
    BALANCE_PAID("잔금 결제완료"),
    RESERVATION_CANCELLED("예약 취소"),
    REFUNDED("결제 취소");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
