package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.Age;
import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.dto.*;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.entity.Participant;
import com.soloProject.myTrip.entity.Payment;
import com.soloProject.myTrip.repository.ItemReservationRepository;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import com.soloProject.myTrip.repository.ParticipantRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.*;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

  @Value("${kakao.admin.key}")
  private String kakaoAdminKey;

  @Value("${iamport.api.key}")
  private String iamportApiKey;

  @Value("${iamport.api.secret}")
  private String iamportApiSecret;

  private final PaymentRepository paymentRepository;
  private final RestTemplate restTemplate;
  private final MemberReservationRepository memberReservationRepository;
  private final ItemReservationRepository itemReservationRepository;
  private final ParticipantRepository participantRepository;

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

      MemberReservation reservation = memberReservationRepository
          .findByReservationNumber(requestDto.getReservationNumber())
          .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

      String userId = reservation.getMember().getEmail();

      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("cid", "TC0ONETIME");
      parameters.add("partner_order_id", orderId);
      parameters.add("partner_user_id", userId);
      parameters.add("item_name", reservation.getItemReservation().getItem().getItemName());
      parameters.add("quantity", "1");
      parameters.add("total_amount", String.valueOf(requestDto.getAmount()));
      parameters.add("tax_free_amount", "0");
      parameters.add("approval_url", "http://localhost:8080/payment/success");
      parameters.add("cancel_url", "http://localhost:8080/payment/cancel");
      parameters.add("fail_url", "http://localhost:8080/payment/fail");

      log.debug("카카오페이 요청 파라미터: {}", parameters);

      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, getHeaders());

      ResponseEntity<ReadyResponse> response = restTemplate.postForEntity(
          "https://kapi.kakao.com/v1/payment/ready",
          requestEntity,
          ReadyResponse.class);

      if (response.getBody() != null) {
        log.info("카카오페이 준비 성공 - TID: {}", response.getBody().getTid());
        // 결제 정보 저장
        paymentInfoMap.put(response.getBody().getTid(), new PaymentInfo(orderId, userId));

        // Payment 데이터 저장
        Payment payment = Payment.builder()
            .itemName(reservation.getItemReservation().getItem().getItemName())
            .paymentKey(response.getBody().getTid())
            .memberReservation(reservation)
            .merchantUid(orderId)
            .amount(requestDto.getAmount())
            .paymentMethod(PaymentMethod.KAKAO)
            .build();

        // 예약 상태 변경
        if (reservation.getReservationStatus().equals(ReservationStatus.RESERVED)) {
          reservation.updateStatus(ReservationStatus.DEPOSIT_PAID);
        } else {
          reservation.updateStatus(ReservationStatus.BALANCE_PAID);
        }
        paymentRepository.save(payment);
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

      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, getHeaders());

      ResponseEntity<ApproveResponse> response = restTemplate.postForEntity(
          "https://kapi.kakao.com/v1/payment/approve",
          requestEntity,
          ApproveResponse.class);

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

  // 카카오페이 결제 취소
  public KakaoCancelResponse kakaoCancel(RefundRequestDto refundRequestDto) {

    log.info("service kakaoCancel............................................");

    MemberReservation reservation = memberReservationRepository
        .findByReservationNumber(refundRequestDto.getReservationNumber()).orElseThrow();
    Payment payment = paymentRepository.findByAmountAndMemberReservationId(refundRequestDto.getAmount(),
        reservation.getId());

    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("cid", "TC0ONETIME");
    parameters.add("tid", payment.getPaymentKey());
    parameters.add("cancel_amount", String.valueOf(refundRequestDto.getAmount()));
    parameters.add("cancel_tax_free_amount", "0");
    parameters.add("cancel_vat_amount", "0");

    // MemberReservation 상태 업데이트
    reservation.updateStatus(ReservationStatus.CANCELLED);
    memberReservationRepository.save(reservation);

    // ItemReservation 좌석 상태 업데이트
    ItemReservation itemReservation = itemReservationRepository.findById(reservation.getItemReservation().getId())
        .orElseThrow(EntityNotFoundException::new);
    List<Participant> adult = participantRepository.findByMemberReservationIdAndAge(reservation.getId(), Age.ADULT);
    List<Participant> child = participantRepository.findByMemberReservationIdAndAge(reservation.getId(), Age.CHILD);
    int cancelledSeat = adult.size() + child.size();
    itemReservation.updateRemainingSeats(cancelledSeat);
    itemReservationRepository.save(itemReservation);

    // 환불 사유 등록
    payment.setRefundReason(refundRequestDto.getReason());

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, getHeaders());

    RestTemplate restTemplate = new RestTemplate();

    return restTemplate.postForObject(
        "https://kapi.kakao.com/v1/payment/cancel",
        requestEntity,
        KakaoCancelResponse.class);
  }

  // 카드결제 준비
  public CardPaymentPrepareResponse prepareCardPayment(PaymentRequestDto requestDto, String userEmail) {
    try {
      log.info("카드결제 준비 시작 - 예약번호: {}", requestDto.getReservationNumber());

      MemberReservation reservation = memberReservationRepository
          .findByReservationNumber(requestDto.getReservationNumber())
          .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

      String merchantUid = "ORDER-CARD-" + UUID.randomUUID().toString();
      log.debug("생성된 주문번호: {}", merchantUid);

      // 결제 정보 저장
      Payment payment = Payment.builder()
          .itemName(reservation.getItemReservation().getItem().getItemName())
          .merchantUid(merchantUid)
          .memberReservation(reservation)
          .amount(requestDto.getAmount())
          .paymentMethod(PaymentMethod.CARD)
          .build();

      paymentRepository.save(payment);
      log.info("결제 정보 저장 완료 - merchantUid: {}", merchantUid);

      return CardPaymentPrepareResponse.builder()
          .merchantUid(merchantUid)
          .itemName(reservation.getItemReservation().getItem().getItemName())
          .buyerEmail(userEmail)
          .buyerName(reservation.getMember().getName())
          .buyerTel(reservation.getMember().getTel())
          .build();

    } catch (Exception e) {
      log.error("카드결제 준비 실패", e);
      throw new RuntimeException("카드결제 준비 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  // 카드결제 검증
  public CardPaymentVerifyResponse verifyCardPayment(CardPaymentVerifyRequest request) {
    try {
      log.info("카드결제 검증 시작 - impUid: {}", request.getImpUid());

      // 아임포트 API 토큰 발급 요청
      String token = getIamportToken();
      log.debug("아임포트 토큰 발급 완료");

      // 아임포트 결제 정보 조회
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);
      HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

      ResponseEntity<IamportResponse> response = restTemplate.exchange(
          "https://api.iamport.kr/payments/" + request.getImpUid(),
          org.springframework.http.HttpMethod.GET,
          requestEntity,
          IamportResponse.class);

      if (response.getBody() == null || response.getBody().getResponse() == null) {
        throw new RuntimeException("결제 정보를 조회할 수 없습니다.");
      }

      IamportPayment iamportPayment = response.getBody().getResponse();
      log.debug("아임포트 결제 정보 조회 완료 - 금액: {}", iamportPayment.getAmount());

      // 결제 금액 검증
      if (!iamportPayment.getAmount().equals(Integer.parseInt(request.getAmount()))) {
        log.error("결제 금액 불일치 - 요청: {}, 실제: {}", request.getAmount(), iamportPayment.getAmount());
        throw new RuntimeException("결제 금액이 일치하지 않습니다.");
      }

      // 결제 정보 업데이트
      Payment payment = paymentRepository.findByMerchantUid(request.getMerchantUid())
          .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

      payment.setPaymentKey(request.getImpUid());
      paymentRepository.save(payment);

      // 예약 상태 업데이트
      MemberReservation reservation = payment.getMemberReservation();
      if (reservation.getReservationStatus().equals(ReservationStatus.RESERVED)) {
        reservation.updateStatus(ReservationStatus.DEPOSIT_PAID);
      } else {
        reservation.updateStatus(ReservationStatus.BALANCE_PAID);
      }
      memberReservationRepository.save(reservation);

      log.info("카드결제 검증 완료 - impUid: {}", request.getImpUid());

      return CardPaymentVerifyResponse.builder()
          .amount(iamportPayment.getAmount())
          .build();

    } catch (Exception e) {
      log.error("카드결제 검증 실패", e);
      throw new RuntimeException("카드결제 검증 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  // 아임포트 토큰 발급
  private String getIamportToken() {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, String> bodyMap = new HashMap<>();
      bodyMap.put("imp_key", iamportApiKey);
      bodyMap.put("imp_secret", iamportApiSecret);

      HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(bodyMap, headers);

      ResponseEntity<IamportTokenResponse> response = restTemplate.postForEntity(
          "https://api.iamport.kr/users/getToken",
          requestEntity,
          IamportTokenResponse.class);

      if (response.getBody() == null || response.getBody().getResponse() == null) {
        throw new RuntimeException("토큰을 발급받을 수 없습니다.");
      }

      return response.getBody().getResponse().getAccess_token();
    } catch (Exception e) {
      log.error("아임포트 토큰 발급 실패", e);
      throw new RuntimeException("아임포트 토큰 발급 중 오류가 발생했습니다: " + e.getMessage());
    }
  }
}
