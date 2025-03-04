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
import org.springframework.transaction.annotation.Isolation;
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
  private String impKey;

  @Value("${iamport.api.secret}")
  private String impSecret;

  private final PaymentRepository paymentRepository;
  private final RestTemplate restTemplate;
  private final MemberReservationRepository memberReservationRepository;
  private final CouponRepository couponRepository;
  private final ItemReservationRepository itemReservationRepository;


  // 결제 준비 정보를 저장할 Map
  private final Map<String, PaymentDto> paymentInfoMap = new HashMap<>();

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public ReadyResponse prepareKakaoPayment(PaymentDto paymentDto) {
    try {
      String orderId = UUID.randomUUID().toString();
      log.info("카카오페이 결제 준비 시작 - 주문번호: {}", orderId);

      MemberReservation reservation = memberReservationRepository
          .findByReservationNumber(paymentDto.getReservationNumber())
          .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

      PaymentType paymentType = reservation.getReservationStatus().equals(ReservationStatus.RESERVED)
          ? PaymentType.DEPOSIT
          : PaymentType.BALANCE;

      // 이미 처리된 결제가 있는지 확인
      synchronized (this) {
        if (paymentRepository.existsByMemberReservationIdAndPaymentType(
            reservation.getId(),
            paymentType)) {
          throw new RuntimeException("이미 처리된 결제가 있습니다.");
        }
      }

      String userId = reservation.getMember().getEmail();

      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("cid", "TC0ONETIME");
      parameters.add("partner_order_id", orderId);
      parameters.add("partner_user_id", userId);
      parameters.add("item_name", reservation.getItemReservation().getItem().getItemName());
      parameters.add("quantity", "1");
      parameters.add("total_amount", String.valueOf(paymentDto.getAmount()));
      parameters.add("tax_free_amount", "0");
      parameters.add("approval_url", "http://localhost:8080/payment/success");
      parameters.add("cancel_url", "http://localhost:8080/payment/cancel");
      parameters.add("fail_url", "http://localhost:8080/payment/fail");

      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, getHeaders());

      ResponseEntity<ReadyResponse> response = restTemplate.postForEntity(
          "https://kapi.kakao.com/v1/payment/ready",
          requestEntity,
          ReadyResponse.class);

      if (response.getBody() != null) {
        log.info("카카오페이 준비 성공 - TID: {}", response.getBody().getTid());
        // 결제 정보 저장 (임시)
        paymentDto.setMerchantUid(orderId);
        paymentDto.setUserId(userId);
        paymentInfoMap.put(response.getBody().getTid(), paymentDto);
      }

      return response.getBody();
    } catch (Exception e) {
      log.error("카카오페이 결제 준비 실패", e);
      throw new RuntimeException("카카오페이 결제 준비 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  // 결제 승인
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public ApproveResponse payApprove(String tid, String pgToken) {
    try {
      log.info("결제 승인 요청 - TID: {}, PG Token: {}", tid, pgToken);

      PaymentDto paymentDto = paymentInfoMap.get(tid);
      if (paymentDto == null) {
        throw new RuntimeException("결제 정보를 찾을 수 없습니다.");
      }

      // 결제 정보가 이미 처리되었는지 확인
      synchronized (this) {
        MemberReservation reservation = memberReservationRepository
            .findByReservationNumber(paymentDto.getReservationNumber())
            .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

        PaymentType paymentType = reservation.getReservationStatus().equals(ReservationStatus.RESERVED)
            ? PaymentType.DEPOSIT
            : PaymentType.BALANCE;

        if (paymentRepository.existsByMemberReservationIdAndPaymentType(
            reservation.getId(),
            paymentType)) {
          throw new RuntimeException("이미 처리된 결제입니다.");
        }
      }

      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("cid", "TC0ONETIME");
      parameters.add("tid", tid);
      parameters.add("partner_order_id", paymentDto.getMerchantUid());
      parameters.add("partner_user_id", paymentDto.getUserId());
      parameters.add("pg_token", pgToken);

      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, getHeaders());

      ResponseEntity<ApproveResponse> response = restTemplate.postForEntity(
          "https://kapi.kakao.com/v1/payment/approve",
          requestEntity,
          ApproveResponse.class);

      if (response.getBody() != null) {
        log.info("카카오페이 승인 성공 - 결제금액: {}", response.getBody().getAmount().getTotal());

        // 결제 데이터 저장 및 예약 상태 변경
        savePaymentAndUpdateStatus(paymentDto, response.getBody());

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

  private void savePaymentAndUpdateStatus(PaymentDto paymentDto, ApproveResponse approveResponse) {
    MemberReservation reservation = memberReservationRepository
        .findByReservationNumber(paymentDto.getReservationNumber())
        .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

    ItemReservation itemReservation = itemReservationRepository.findById(reservation.getItemReservation().getId()).orElseThrow(EntityNotFoundException::new);

    // Payment 데이터 저장
    Payment payment = Payment.builder()
        .itemName(reservation.getItemReservation().getItem().getItemName())
        .paymentKey(approveResponse.getTid())
        .memberReservation(reservation)
        .merchantUid(paymentDto.getMerchantUid())
        .amount(paymentDto.getAmount())
        .paymentMethod(PaymentMethod.KAKAO)
        .usedMileage(paymentDto.getUsedMileage())
        .usedCoupon(null)
        .build();

    // 쿠폰 처리
    if (paymentDto.getCouponId() != null) {
      Coupon coupon = couponRepository.findById(paymentDto.getCouponId())
          .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다."));
      payment.setUsedCoupon(coupon);
      coupon.setCouponStatus(CouponStatus.USED);
    }

    // 마일리지 처리
    if (paymentDto.getUsedMileage() != null && paymentDto.getUsedMileage() > 0) {
      payment.getMemberReservation().getMember().useMileage(paymentDto.getUsedMileage());
    }

    // 예약 상태 변경
    if (reservation.getReservationStatus().equals(ReservationStatus.RESERVED)) {
      reservation.updateStatus(ReservationStatus.DEPOSIT_PAID);
      payment.setPaymentType(PaymentType.DEPOSIT);
    } else {
      reservation.updateStatus(ReservationStatus.BALANCE_PAID);
      payment.setPaymentType(PaymentType.BALANCE);

      //해당 날짜의 예약이 모두 잔금 지불 상태라면 예약 마감
      boolean flag = true;
      for(MemberReservation memberReservation : itemReservation.getMemberReservations()){
        if(!memberReservation.getReservationStatus().equals(ReservationStatus.BALANCE_PAID)){
          flag = false;
          break;
        }
      }
      if(flag){
        itemReservation.soldOutReservation();
      }
    }

    paymentRepository.save(payment);
  }

  // 카카오페이 측에 요청 시 헤더부에 필요한 값
  public HttpHeaders getHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    return headers;
  }

  // 카드결제 준비
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public CardPaymentPrepareResponse prepareCardPayment(PaymentDto requestDto, String userEmail) {
    try {
      log.info("카드결제 준비 시작 - 예약번호: {}", requestDto.getReservationNumber());

      MemberReservation reservation = memberReservationRepository
          .findByReservationNumber(requestDto.getReservationNumber())
          .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

      PaymentType paymentType = reservation.getReservationStatus().equals(ReservationStatus.RESERVED)
          ? PaymentType.DEPOSIT
          : PaymentType.BALANCE;

      // 이미 처리된 결제가 있는지 확인
      synchronized (this) {
        if (paymentRepository.existsByMemberReservationIdAndPaymentType(
            reservation.getId(),
            paymentType)) {
          throw new RuntimeException("이미 처리된 결제가 있습니다.");
        }
      }

      String merchantUid = "ORDER-CARD-" + UUID.randomUUID().toString();
      log.debug("생성된 주문번호: {}", merchantUid);

      // 결제 정보를 임시로 저장 (메모리에만)
      PaymentDto paymentDto = PaymentDto.builder()
          .reservationNumber(requestDto.getReservationNumber())
          .amount(requestDto.getAmount())
          .couponId(requestDto.getCouponId())
          .usedMileage(requestDto.getUsedMileage())
          .merchantUid(merchantUid)
          .build();

      // 결제 정보를 Map에 임시 저장
      paymentInfoMap.put(merchantUid, paymentDto);
      log.info("결제 정보 저장 완료 - merchantUid: {}, amount: {}", merchantUid, paymentDto.getAmount());

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
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public CardPaymentVerifyResponse verifyCardPayment(CardPaymentVerifyRequest request) {
    try {
      log.info("카드결제 검증 시작 - impUid: {}, merchantUid: {}", request.getImpUid(), request.getMerchantUid());

      // 결제 준비 정보 조회
      PaymentDto paymentDto = paymentInfoMap.get(request.getMerchantUid());
      if (paymentDto == null) {
        log.error("결제 정보를 찾을 수 없음 - merchantUid: {}", request.getMerchantUid());
        throw new RuntimeException("결제 정보를 찾을 수 없습니다.");
      }

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

      // 결제 상태 확인
      if (iamportPayment.getStatus().equals("paid")) {
        MemberReservation reservation = memberReservationRepository
            .findByReservationNumber(paymentDto.getReservationNumber())
            .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

        ItemReservation itemReservation = itemReservationRepository.findById(reservation.getItemReservation().getId()).orElseThrow(EntityNotFoundException::new);

        // 결제 정보 저장
        Payment payment = Payment.builder()
            .itemName(reservation.getItemReservation().getItem().getItemName())
            .merchantUid(request.getMerchantUid())
            .memberReservation(reservation)
            .amount(paymentDto.getAmount())
            .paymentMethod(PaymentMethod.CARD)
            .paymentType(reservation.getReservationStatus().equals(ReservationStatus.RESERVED)
                ? PaymentType.DEPOSIT
                : PaymentType.BALANCE)
            .paymentKey(request.getImpUid())
            .usedMileage(paymentDto.getUsedMileage())
            .usedCoupon(null)
            .build();

        // 쿠폰 처리
        if (paymentDto.getCouponId() != null) {
          Coupon coupon = couponRepository.findById(paymentDto.getCouponId())
              .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다."));
          payment.setUsedCoupon(coupon);
          coupon.setCouponStatus(CouponStatus.USED);
        }

        // 마일리지 처리
        if (paymentDto.getUsedMileage() != null && paymentDto.getUsedMileage() > 0) {
          reservation.getMember().useMileage(paymentDto.getUsedMileage());
        }

        // 예약 상태 업데이트
        if (reservation.getReservationStatus().equals(ReservationStatus.RESERVED)) {
          reservation.updateStatus(ReservationStatus.DEPOSIT_PAID);
        } else {
          reservation.updateStatus(ReservationStatus.BALANCE_PAID);
          boolean flag = true;
          for(MemberReservation memberReservation : itemReservation.getMemberReservations()){
            if(!memberReservation.getReservationStatus().equals(ReservationStatus.BALANCE_PAID)){
              flag = false;
              break;
            }
          }
          if(flag){
            itemReservation.soldOutReservation();
          }
        }

        // 모든 변경사항 저장
        paymentRepository.save(payment);
        memberReservationRepository.save(reservation);

        // 임시 저장된 결제 정보 제거
        paymentInfoMap.remove(request.getMerchantUid());

        log.info("카드결제 검증 완료 - impUid: {}", request.getImpUid());

        return CardPaymentVerifyResponse.builder()
            .amount(iamportPayment.getAmount())
            .build();
      } else {
        throw new RuntimeException("결제가 완료되지 않았습니다. 상태: " + iamportPayment.getStatus());
      }

    } catch (Exception e) {
      log.error("카드결제 검증 실패", e);
      throw new RuntimeException("카드결제 검증 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

  // 아임포트 토큰 발급
  public String getIamportToken() {
    try {
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
      parameters.add("imp_key", impKey);
      parameters.add("imp_secret", impSecret);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);

      ResponseEntity<Map> response = restTemplate.postForEntity(
          "https://api.iamport.kr/users/getToken",
          requestEntity,
          Map.class);

      if (response.getBody() != null && response.getBody().get("response") != null) {
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody().get("response");
        return (String) responseBody.get("access_token");
      } else {
        throw new RuntimeException("아임포트 토큰 발급 실패");
      }
    } catch (Exception e) {
      log.error("아임포트 토큰 발급 실패", e);
      throw new RuntimeException("아임포트 토큰 발급 중 오류가 발생했습니다: " + e.getMessage());
    }
  }

}
