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
  // 기본 결제 정보
  private Long id;
  private Integer amount;
  private PaymentType paymentType;
  private PaymentMethod paymentMethod;
  private String paymentKey;
  private String merchantUid;

  // 결제 요청 정보
  private String reservationNumber;
  private Long couponId;
  private Integer usedMileage;

  // 결제 상태 정보
  private String status;
  private String errorMessage;

  // 사용자 정보
  private String userId;

  // DTO 변환 메서드
  public static PaymentDto fromEntity(Payment payment) {
    return PaymentDto.builder()
        .id(payment.getId())
        .amount(payment.getAmount())
        .paymentType(payment.getPaymentType())
        .paymentMethod(payment.getPaymentMethod())
        .paymentKey(payment.getPaymentKey())
        .merchantUid(payment.getMerchantUid())
        .build();
  }

  public static List<PaymentDto> fromEntityList(List<Payment> payments) {
    return payments.stream()
        .map(PaymentDto::fromEntity)
        .collect(Collectors.toList());
  }

  // 결제 준비용 DTO 생성 메서드
  public static PaymentDto createPrepareDto(String reservationNumber, Integer amount,
      Long couponId, Integer usedMileage) {
    return PaymentDto.builder()
        .reservationNumber(reservationNumber)
        .amount(amount)
        .couponId(couponId)
        .usedMileage(usedMileage)
        .build();
  }

  public static List<PaymentDto> createDtoList(List<Payment> payments) {
    return payments.stream()
        .map(payment -> {
          PaymentDto dto = new PaymentDto();
          dto.setId(payment.getId());
          dto.setAmount(payment.getAmount());
          dto.setPaymentType(payment.getPaymentType());
          dto.setPaymentMethod(payment.getPaymentMethod());
          return dto;
        })
        .collect(Collectors.toList());
  }
}