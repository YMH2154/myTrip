package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.PaymentType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundRequestDto {
    private Long paymentId;
    private Integer amount;
    private String reason;
    private boolean flag;
    private PaymentType paymentType;
    private String merchantUid;
    private String impUid;
}