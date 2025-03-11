package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.PaymentType;
import com.soloProject.myTrip.constant.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSearchDto {
    private String searchBy; // 검색 조건 (email, itemName, merchantUid)
    private String searchQuery; // 검색어
    private String paymentMethod; // 결제 수단
    private String reservationStatus; // 결제 상태
    private String paymentType; // 결제 종류
}
