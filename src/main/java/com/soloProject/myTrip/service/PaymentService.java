package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.*;
import com.soloProject.myTrip.dto.*;
import com.soloProject.myTrip.entity.*;
import com.soloProject.myTrip.repository.*;
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
  private final CouponRepository couponRepository;

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
            .usedMileage(0)
            .usedCoupon(null)
            .paymentType(null)
            .build();

        // 쿠폰 처리
        if (requestDto.getCouponId() != null) {
          Coupon coupon = couponRepository.findById(requestDto.getCouponId())
                  .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다."));
          payment.setUsedCoupon(coupon);
          coupon.setCouponStatus(CouponStatus.USED);
        }

        // 마일리지 처리
        Integer usedMileage = requestDto.getUsedMileage();
        if (usedMileage != null && usedMileage > 0) {
          payment.setUsedMileage(usedMileage);
          payment.getMemberReservation().getMember().useMileage(usedMileage);
        }

        // 예약 상태 변경
        if (reservation.getReservationStatus().equals(ReservationStatus.RESERVED)) {
          reservation.updateStatus(ReservationStatus.DEPOSIT_PAID);
          payment.setPaymentType(PaymentType.DEPOSIT);
        } else {
          reservation.updateStatus(ReservationStatus.BALANCE_PAID);
          payment.setPaymentType(PaymentType.BALANCE);
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

    Payment payment = paymentRepository.findById(refundRequestDto.getPaymentId())
        .orElseThrow(EntityNotFoundException::new);

    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("cid", "TC0ONETIME");
    parameters.add("tid", payment.getPaymentKey());
    parameters.add("cancel_amount", String.valueOf(refundRequestDto.getAmount()));
    parameters.add("cancel_tax_free_amount", "0");
    parameters.add("cancel_vat_amount", "0");

    cancelPayment(payment, refundRequestDto);

    HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, getHeaders());

    RestTemplate restTemplate = new RestTemplate();

    return restTemplate.postForObject(
        "https://kapi.kakao.com/v1/payment/cancel",
        requestEntity,
        KakaoCancelResponse.class);
  }

  // 포트원 결제 취소
  public IamportResponse cancelIamportPayment(RefundRequestDto refundRequestDto) {
    try {
      log.info("포트원 결제 취소 시작 - paymentId: {}", refundRequestDto.getPaymentId());

      Payment payment = paymentRepository.findById(refundRequestDto.getPaymentId())
          .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

      // 아임포트 API 토큰 발급
      String token = getIamportToken();
      log.debug("아임포트 토큰 발급 완료");

      // 결제 취소 요청 데이터 준비
      Map<String, Object> cancelData = new HashMap<>();
      cancelData.put("imp_uid", payment.getPaymentKey());
      cancelData.put("amount", refundRequestDto.getAmount());
      cancelData.put("reason", refundRequestDto.getReason());

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(token);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(cancelData, headers);

      ResponseEntity<IamportResponse> response = restTemplate.exchange(
          "https://api.iamport.kr/payments/cancel",
          org.springframework.http.HttpMethod.POST,
          requestEntity,
          IamportResponse.class);

      if (response.getBody() == null || response.getBody().getResponse() == null) {
        throw new RuntimeException("결제 취소에 실패했습니다.");
      }

      // 결제 취소 후 데이터 업데이트
      cancelPayment(payment, refundRequestDto);
      log.info("포트원 결제 취소 완료 - impUid: {}", payment.getPaymentKey());

      return response.getBody();

    } catch (Exception e) {
      log.error("포트원 결제 취소 실패", e);
      throw new RuntimeException("결제 취소 중 오류가 발생했습니다: " + e.getMessage());
    }
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
          .usedMileage(0)
          .usedCoupon(null)
          .build();

      // 쿠폰 처리
      if (requestDto.getCouponId() != null) {
        Coupon coupon = couponRepository.findById(requestDto.getCouponId())
                .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다."));
        payment.setUsedCoupon(coupon);
        coupon.setCouponStatus(CouponStatus.USED);
      }

      // 마일리지 처리
      Integer usedMileage = requestDto.getUsedMileage();
      if (usedMileage != null && usedMileage > 0) {
        payment.setUsedMileage(usedMileage);
        payment.getMemberReservation().getMember().useMileage(usedMileage);
      }

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

  // 결제 취소 시 데이터 업데이트 메서드
  private void cancelPayment(Payment payment, RefundRequestDto refundRequestDto) {
    Integer usedMileage = payment.getUsedMileage();
    Coupon usedCoupon = couponRepository.findById(payment.getUsedCoupon().getId())
        .orElseThrow(EntityNotFoundException::new);
    MemberReservation reservation = payment.getMemberReservation();

    // MemberReservation 상태 업데이트
    reservation.updateStatus(ReservationStatus.CANCELLED);

    // ItemReservation 좌석 상태 업데이트
    ItemReservation itemReservation = itemReservationRepository.findById(reservation.getItemReservation().getId())
        .orElseThrow(EntityNotFoundException::new);
    List<Participant> adult = participantRepository.findByMemberReservationIdAndAge(reservation.getId(), Age.ADULT);
    List<Participant> child = participantRepository.findByMemberReservationIdAndAge(reservation.getId(), Age.CHILD);
    int cancelledSeat = adult.size() + child.size();
    itemReservation.updateRemainingSeats(cancelledSeat);

    // 취소 사유 등록
    payment.setCancelReason(refundRequestDto.getReason());

    // 사용한 쿠폰 및 마일리지 반환
    payment.getMemberReservation().getMember().cancelMileage(usedMileage);
    if (usedCoupon != null) {
      usedCoupon.setCouponStatus(CouponStatus.USABLE);
    }
  }

  public Payment getPayment(String reservationNumber) {
    MemberReservation reservation = memberReservationRepository.findByReservationNumber(reservationNumber)
        .orElseThrow(EntityNotFoundException::new);
    Payment payment;

    if (reservation.getReservationStatus().equals(ReservationStatus.DEPOSIT_PAID)) {
      payment = paymentRepository.findByMemberReservationIdAndPaymentType(reservation.getId(), PaymentType.DEPOSIT);
    } else {
      payment = paymentRepository.findByMemberReservationIdAndPaymentType(reservation.getId(), PaymentType.BALANCE);
    }

    return payment;

  }
}
