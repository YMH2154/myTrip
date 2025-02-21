package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.entity.PaymentData;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import com.soloProject.myTrip.repository.PaymentDataRepository;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final PaymentDataRepository paymentDataRepository;
    private final RestTemplate restTemplate;

    @Value("${kakao.admin.key}")
    private String kakaoAdminKey;

    @Value("${naver.client.id}")
    private String naverClientId;

    @Value("${naver.client.secret}")
    private String naverClientSecret;

    @Value("${toss.secret.key}")
    private String tossSecretKey;

    // 카카오페이 결제 준비
    public String prepareKakaoPayment(MemberReservation reservation) {
        String orderNumber = generateOrderNumber();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");

        JSONObject params = new JSONObject();
        params.put("cid", "TC0ONETIME");  // 가맹점 코드
        params.put("partner_order_id", orderNumber);
        params.put("partner_user_id", reservation.getMember().getEmail());
        params.put("item_name", reservation.getItemReservation().getItem().getItemName());
        params.put("quantity", 1);
        params.put("total_amount", reservation.getTotalDeposit());
        params.put("tax_free_amount", 0);
        params.put("approval_url", "http://localhost:8080/payment/kakao/success");
        params.put("cancel_url", "http://localhost:8080/payment/kakao/cancel");
        params.put("fail_url", "http://localhost:8080/payment/kakao/fail");

        HttpEntity<String> entity = new HttpEntity<>(params.toString(), headers);

        // 카카오페이 결제 준비 API 호출
        String response = restTemplate.postForObject(
            "https://kapi.kakao.com/v1/payment/ready",
            entity,
            String.class
        );

        savePaymentData(reservation, orderNumber, PaymentMethod.KAKAO);
        
        return response;  // 결제 페이지 URL 반환
    }

    // 네이버페이 결제 준비
    public String prepareNaverPayment(MemberReservation reservation) {
        String orderNumber = generateOrderNumber();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-NaverPay-Chain-Id", naverClientId);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject params = new JSONObject();
        // 네이버페이 결제 파라미터 설정
        params.put("merchantPayKey", orderNumber);
        params.put("productName", reservation.getItemReservation().getItem().getItemName());
        params.put("totalPayAmount", reservation.getTotalDeposit());
        
        HttpEntity<String> entity = new HttpEntity<>(params.toString(), headers);
        
        // 네이버페이 결제 준비 API 호출
        String response = restTemplate.postForObject(
            "https://dev.apis.naver.com/naverpay-partner/naverpay/payments/v2/reserve",
            entity,
            String.class
        );

        savePaymentData(reservation, orderNumber, PaymentMethod.NAVER);
        
        return response;
    }

    // 토스페이먼츠 결제 준비
    public String prepareTossPayment(MemberReservation reservation) {
        String orderNumber = generateOrderNumber();
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + tossSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject params = new JSONObject();
        params.put("amount", reservation.getTotalDeposit());
        params.put("orderId", orderNumber);
        params.put("orderName", reservation.getItemReservation().getItem().getItemName());
        params.put("successUrl", "http://localhost:8080/payment/toss/success");
        params.put("failUrl", "http://localhost:8080/payment/toss/fail");
        
        HttpEntity<String> entity = new HttpEntity<>(params.toString(), headers);
        
        // 토스페이먼츠 결제 준비 API 호출
        String response = restTemplate.postForObject(
            "https://api.tosspayments.com/v1/payments",
            entity,
            String.class
        );

        savePaymentData(reservation, orderNumber, PaymentMethod.TOSS);
        
        return response;
    }

    private String generateOrderNumber() {
        return UUID.randomUUID().toString();
    }

    private void savePaymentData(MemberReservation reservation, String orderNumber, PaymentMethod method) {
        PaymentData paymentData = new PaymentData();
        paymentData.setMember(reservation.getMember());
        paymentData.setOrderNumber(orderNumber);
        paymentData.setReservationStatus(ReservationStatus.PAID);
        paymentData.setPaymentMethod(method);
        paymentDataRepository.save(paymentData);
    }
}
