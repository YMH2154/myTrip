package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardPaymentDto {
    private String reservationNumber;
    private Integer amount;
    private Long couponId;
    private Integer usedMileage;
    private String impUid;
    private String merchantUid;
    private String paymentStatus;
    private String cardName;
    private String cardNumber;
    private Integer cardQuota;
    private String buyerName;
    private String buyerEmail;
    private String buyerTel;
    private String failReason;
    private String cancelReason;
} 