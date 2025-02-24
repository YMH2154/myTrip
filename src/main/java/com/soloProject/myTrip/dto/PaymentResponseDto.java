package com.soloProject.myTrip.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
  private String redirectUrl; // 결제 페이지 URL
  private String paymentKey; // 결제 고유 키
  private String merchantUid; // 주문 번호
  private String message; // 응답 메시지
  private boolean success; // 성공 여부

  // 카카오페이 응답을 위한 필드
  private String next_redirect_pc_url;

  public String getRedirectUrl() {
    return next_redirect_pc_url != null ? next_redirect_pc_url : redirectUrl;
  }
}