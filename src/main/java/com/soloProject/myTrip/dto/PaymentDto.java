package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.PaymentType;
import com.soloProject.myTrip.entity.Payment;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
  // 공통 필드
  private Long id;
  private String reservationNumber;
  private Integer amount;
  private Long couponId;
  private Integer usedMileage;
  private String merchantUid;
  private String status;
  private String errorMessage;
  private String userId;

  // 결제 타입 및 수단
  private PaymentType paymentType;
  private PaymentMethod paymentMethod;
  private String paymentKey;

  // 카드 결제 관련 필드
  private String impUid;
  private String cardName;
  private String cardNumber;
  private Integer cardQuota;
  private String buyerName;
  private String buyerEmail;
  private String buyerTel;
  private String failReason;
  private String cancelReason;

  public static List<PaymentDto> createDtoList(List<Payment> payments) {
    return payments.stream()
        .map(payment -> {
          PaymentDto dto = new PaymentDto();
          dto.setId(payment.getId());
          dto.setAmount(payment.getAmount());
          dto.setPaymentType(payment.getPaymentType());
          dto.setPaymentMethod(payment.getPaymentMethod());
          dto.setPaymentKey(payment.getPaymentKey());
          dto.setMerchantUid(payment.getMerchantUid());
          return dto;
        })
        .collect(Collectors.toList());
  }
}