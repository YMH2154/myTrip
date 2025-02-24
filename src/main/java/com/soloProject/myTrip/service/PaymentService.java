package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.dto.PaymentRequestDto;
import com.soloProject.myTrip.dto.PaymentResponseDto;
import com.soloProject.myTrip.exception.PaymentException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import com.soloProject.myTrip.entity.Payment;
import com.soloProject.myTrip.entity.MemberReservation;

import com.soloProject.myTrip.repository.PaymentRepository;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final MemberReservationRepository reservationRepository;
  private final RestTemplate restTemplate;

  @Value("${toss.secret.key}")
  private String tossSecretKey;

  @Value("${naver.client.id}")
  private String naverClientKey;

  @Value("${naver.client.secret}")
  private String naverClientSecret;

  @Value("${kakao.admin.key}")
  private String kakaoClientKey;

  @Value("${payment.success.url}")
  private String successUrl;

  @Value("${payment.fail.url}")
  private String failUrl;

  @Value("${payment.cancel.url}")
  private String cancelUrl;

  @Transactional
  public PaymentResponseDto preparePayment(PaymentRequestDto requestDto) {
    try {
      log.info("Preparing payment for reservation: {}", requestDto.getReservationNumber());

      // 예약 정보 조회 및 검증
      MemberReservation reservation = validateAndGetReservation(requestDto);

      // 결제 정보 생성 및 저장
      Payment payment = createAndSavePayment(requestDto, reservation);

      // 결제 수단별 처리
      PaymentResponseDto response = processPaymentByMethod(payment, requestDto);

      // 결제 정보 업데이트
      updatePaymentInfo(payment, response);

      return response;

    } catch (EntityNotFoundException e) {
      log.error("Reservation not found: {}", requestDto.getReservationNumber());
      throw e;
    } catch (IllegalArgumentException e) {
      log.error("Invalid payment amount for reservation: {}", requestDto.getReservationNumber());
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during payment preparation", e);
      throw new PaymentException("결제 준비 중 오류가 발생했습니다");
    }
  }

  private MemberReservation validateAndGetReservation(PaymentRequestDto requestDto) {
    MemberReservation reservation = reservationRepository.findByReservationNumber(requestDto.getReservationNumber())
        .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

    validatePaymentAmount(reservation, requestDto.getAmount());
    validateReservationStatus(reservation);

    return reservation;
  }

  private void validatePaymentAmount(MemberReservation reservation, int requestAmount) {
    int expectedAmount = reservation.getReservationStatus() == ReservationStatus.RESERVED
        ? reservation.getTotalDeposit()
        : reservation.getTotalPrice();

    if (requestAmount != expectedAmount) {
      throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
    }
  }

  private void validateReservationStatus(MemberReservation reservation) {
    if (reservation.getReservationStatus() == ReservationStatus.CANCELLED
        || reservation.getReservationStatus() == ReservationStatus.REFUNDED) {
      throw new IllegalStateException("취소되거나 환불된 예약은 결제할 수 없습니다.");
    }
  }

  private Payment createAndSavePayment(PaymentRequestDto requestDto, MemberReservation reservation) {
    Payment payment = Payment.builder()
        .memberReservation(reservation)
        .paymentMethod(requestDto.getPaymentMethod())
        .merchantUid(generateMerchantUid())
        .price(requestDto.getAmount())
        .itemName(requestDto.getItemName())
        .build();

    return paymentRepository.save(payment);
  }

  private PaymentResponseDto processPaymentByMethod(Payment payment, PaymentRequestDto requestDto) {
    return switch (requestDto.getPaymentMethod()) {
      case KAKAO -> processKakaoPayment(payment, requestDto);
      case NAVER -> processNaverPayment(payment, requestDto);
      case TOSS -> processTossPayment(payment, requestDto);
    };
  }

  private PaymentResponseDto processKakaoPayment(Payment payment, PaymentRequestDto requestDto) {
    HttpHeaders headers = createKakaoHeaders();
    Map<String, Object> requestBody = createKakaoPayRequestBody(payment, requestDto);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<KakaoPayReadyResponse> response = restTemplate.postForEntity(
        "https://kapi.kakao.com/v1/payment/ready",
        entity,
        KakaoPayReadyResponse.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new PaymentException("카카오페이 결제 준비 요청 실패");
    }

    // 카카오페이 결제 고유번호 저장
    payment.setPaymentKey(response.getBody().getTid());
    paymentRepository.save(payment);

    return PaymentResponseDto.builder()
        .redirectUrl(response.getBody().getNext_redirect_pc_url())
        .merchantUid(payment.getMerchantUid())
        .success(true)
        .message("카카오페이 결제 준비가 완료되었습니다.")
        .build();
  }

  private HttpHeaders createKakaoHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "KakaoAK " + kakaoClientKey);
    headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    return headers;
  }

  private Map<String, Object> createKakaoPayRequestBody(Payment payment, PaymentRequestDto requestDto) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("cid", "TC0ONETIME");
    requestBody.put("partner_order_id", payment.getMerchantUid());
    requestBody.put("partner_user_id", requestDto.getBuyerEmail());
    requestBody.put("item_name", payment.getItemName());
    requestBody.put("quantity", 1);
    requestBody.put("total_amount", payment.getPrice());
    requestBody.put("tax_free_amount", 0);
    requestBody.put("approval_url", successUrl);
    requestBody.put("cancel_url", cancelUrl);
    requestBody.put("fail_url", failUrl);
    return requestBody;
  }

  private PaymentResponseDto processNaverPayment(Payment payment, PaymentRequestDto requestDto) {
    HttpHeaders headers = createNaverHeaders();
    Map<String, Object> requestBody = createNaverPayRequestBody(payment, requestDto);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<NaverPayReadyResponse> response = restTemplate.postForEntity(
        "https://dev.apis.naver.com/naverpay-partner/naverpay/payments/v2/reserve",
        entity,
        NaverPayReadyResponse.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new PaymentException("네이버페이 결제 준비 요청 실패");
    }

    // 네이버페이 결제 고유번호 저장
    payment.setPaymentKey(response.getBody().getPaymentId());
    paymentRepository.save(payment);

    return PaymentResponseDto.builder()
        .redirectUrl(response.getBody().getPaymentUrl())
        .merchantUid(payment.getMerchantUid())
        .success(true)
        .message("네이버페이 결제 준비가 완료되었습니다.")
        .build();
  }

  private HttpHeaders createNaverHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Naver-Client-Id", naverClientKey);
    headers.add("X-Naver-Client-Secret", naverClientSecret);
    headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    return headers;
  }

  private Map<String, Object> createNaverPayRequestBody(Payment payment, PaymentRequestDto requestDto) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("merchantPayKey", payment.getMerchantUid());
    requestBody.put("productName", payment.getItemName());
    requestBody.put("totalPayAmount", payment.getPrice());
    requestBody.put("taxScopeAmount", payment.getPrice());
    requestBody.put("taxExScopeAmount", 0);
    requestBody.put("returnUrl", successUrl);
    requestBody.put("failUrl", failUrl);

    Map<String, Object> productItems = new HashMap<>();
    productItems.put("categoryType", "TRAVEL");
    productItems.put("categoryId", "TRAVEL");
    productItems.put("uid", payment.getMerchantUid());
    productItems.put("name", payment.getItemName());
    productItems.put("count", 1);
    productItems.put("payAmount", payment.getPrice());

    requestBody.put("productItems", List.of(productItems));
    return requestBody;
  }

  private PaymentResponseDto processTossPayment(Payment payment, PaymentRequestDto requestDto) {
    HttpHeaders headers = createTossHeaders();
    Map<String, Object> requestBody = createTossPayRequestBody(payment, requestDto);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    ResponseEntity<TossPaymentResponse> response = restTemplate.postForEntity(
        "https://api.tosspayments.com/v1/payments",
        entity,
        TossPaymentResponse.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new PaymentException("토스페이먼츠 결제 준비 요청 실패");
    }

    // 토스 결제 고유번호 저장
    payment.setPaymentKey(response.getBody().getPaymentKey());
    paymentRepository.save(payment);

    return PaymentResponseDto.builder()
        .redirectUrl(response.getBody().getCheckoutUrl())
        .merchantUid(payment.getMerchantUid())
        .success(true)
        .message("토스페이먼츠 결제 준비가 완료되었습니다.")
        .build();
  }

  private HttpHeaders createTossHeaders() {
    HttpHeaders headers = new HttpHeaders();
    String encodedAuth = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
    headers.add("Authorization", "Basic " + encodedAuth);
    headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    return headers;
  }

  private Map<String, Object> createTossPayRequestBody(Payment payment, PaymentRequestDto requestDto) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("amount", payment.getPrice());
    requestBody.put("orderId", payment.getMerchantUid());
    requestBody.put("orderName", payment.getItemName());
    requestBody.put("successUrl", successUrl);
    requestBody.put("failUrl", failUrl);
    requestBody.put("customerName", requestDto.getBuyerName());
    requestBody.put("customerEmail", requestDto.getBuyerEmail());

    Map<String, String> cardOptions = new HashMap<>();
    cardOptions.put("cardCompany", null); // 카드사 직접 선택 옵션
    cardOptions.put("cardInstallmentPlan", null); // 할부 개월 수 선택 옵션
    requestBody.put("cardOptions", cardOptions);

    return requestBody;
  }

  private void updatePaymentInfo(Payment payment, PaymentResponseDto response) {
    payment.setPaymentKey(response.getPaymentKey());
    paymentRepository.save(payment);
  }

  private String generateMerchantUid() {
    return "ORD_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
  }

  @Transactional
  public PaymentResponseDto completePayment(String paymentKey, String orderId, Integer amount) {
    Payment payment = paymentRepository.findByMerchantUid(orderId)
        .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

    if (payment.getPrice() != amount) {
      throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
    }

    // 결제 완료 시 최종 결제 키 업데이트 (필요한 경우)
    if (!payment.getPaymentKey().equals(paymentKey)) {
      payment.setPaymentKey(paymentKey);
    }

    MemberReservation reservation = payment.getMemberReservation();
    if (reservation.getReservationStatus() == ReservationStatus.RESERVED) {
      reservation.setReservationStatus(ReservationStatus.DEPOSIT_PAID);
    } else {
      reservation.setReservationStatus(ReservationStatus.BALANCE_PAID);
    }

    return PaymentResponseDto.builder()
        .success(true)
        .message("결제가 완료되었습니다.")
        .paymentKey(paymentKey)
        .merchantUid(orderId)
        .build();
  }

  @Getter
  @Setter
  private static class KakaoPayReadyResponse {
    private String tid; // 결제 고유번호
    private String next_redirect_pc_url; // PC 결제 페이지 URL
  }

  @Getter
  @Setter
  private static class NaverPayReadyResponse {
    private String paymentId; // 결제 고유번호
    private String paymentUrl; // 결제 페이지 URL
    private String code;
    private String message;
  }

  @Getter
  @Setter
  private static class TossPaymentResponse {
    private String paymentKey; // 결제 고유번호
    private String checkoutUrl; // 결제 페이지 URL
    private String status;
  }
}
