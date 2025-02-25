package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ApproveResponse;
import com.soloProject.myTrip.dto.PaymentRequestDto;
import com.soloProject.myTrip.dto.ReadyResponse;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import com.soloProject.myTrip.repository.PaymentRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

  @Value("${kakao.admin.key}")
  private String kakaoAdminKey;

  @Value("${naver.client.id}")
  private String naverClientId;

  @Value("${naver.client.secret}")
  private String naverClientSecret;

  @Value("${toss.secret.key}")
  private String tossSecretKey;

  private final PaymentRepository paymentRepository;
  private final RestTemplate restTemplate;
  private final MemberReservationRepository memberReservationRepository;

  // 결제 준비 정보를 저장할 Map
  private final Map<String, PaymentInfo> paymentInfoMap = new HashMap<>();

  @Getter
  @Setter
  @AllArgsConstructor
  private static class PaymentInfo {
    private String orderId;
    private String userId;
  }

  public ReadyResponse prepareKakaoPayment(PaymentRequestDto requestDto) {
    try {
      String orderId = UUID.randomUUID().toString();
      log.info("카카오페이 결제 준비 시작 - 주문번호: {}", orderId);

      MemberReservation reservation = memberReservationRepository.findByReservationNumber(requestDto.getReservationNumber())
              .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

      String userId = reservation.getMember().getEmail();

      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("cid", "TC0ONETIME");
      parameters.add("partner_order_id", orderId);
      parameters.add("partner_user_id", userId);
      parameters.add("item_name", reservation.getItemReservation().getItem().getItemName());
      parameters.add("quantity", "1");
      parameters.add("total_amount", requestDto.getAmount());
      parameters.add("tax_free_amount", "0");
      parameters.add("approval_url", "http://localhost:8080/payment/success");
      parameters.add("cancel_url", "http://localhost:8080/payment/cancel");
      parameters.add("fail_url", "http://localhost:8080/payment/fail");

      log.debug("카카오페이 요청 파라미터: {}", parameters);

      HttpEntity<MultiValueMap<String, String>> requestEntity = 
              new HttpEntity<>(parameters, getHeaders());

      ResponseEntity<ReadyResponse> response = restTemplate.postForEntity(
              "https://kapi.kakao.com/v1/payment/ready",
              requestEntity,
              ReadyResponse.class
      );

      if (response.getBody() != null) {
        log.info("카카오페이 준비 성공 - TID: {}", response.getBody().getTid());
        // 결제 정보 저장
        paymentInfoMap.put(response.getBody().getTid(), new PaymentInfo(orderId, userId));
      }

      return response.getBody();
    } catch (Exception e) {
      log.error("카카오페이 결제 준비 실패", e);
      throw new RuntimeException("카카오페이 결제 준비 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  public ApproveResponse payApprove(String tid, String pgToken) {
    try {
        log.info("결제 승인 요청 - TID: {}, PG Token: {}", tid, pgToken);

        // 저장된 결제 정보 조회
        PaymentInfo paymentInfo = paymentInfoMap.get(tid);
        if (paymentInfo == null) {
            throw new RuntimeException("결제 정보를 찾을 수 없습니다.");
        }

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("cid", "TC0ONETIME");
        parameters.add("tid", tid);
        parameters.add("partner_order_id", paymentInfo.getOrderId());
        parameters.add("partner_user_id", paymentInfo.getUserId());
        parameters.add("pg_token", pgToken);

        log.debug("카카오페이 승인 요청 파라미터: {}", parameters);

        HttpEntity<MultiValueMap<String, String>> requestEntity = 
                new HttpEntity<>(parameters, getHeaders());

        ResponseEntity<ApproveResponse> response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v1/payment/approve",
                requestEntity,
                ApproveResponse.class
        );

        if (response.getBody() != null) {
            log.info("카카오페이 승인 성공 - 결제금액: {}", response.getBody().getAmount().getTotal());
            // 사용한 결제 정보 제거
            paymentInfoMap.remove(tid);
            return response.getBody();
        } else {
            throw new RuntimeException("카카오페이 승인 응답이 비어있습니다.");
        }
    } catch (Exception e) {
        log.error("카카오페이 승인 실패", e);
        throw new RuntimeException("카카오페이 승인 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  // 카카오페이 측에 요청 시 헤더부에 필요한 값
  private HttpHeaders getHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    return headers;
  }
}
