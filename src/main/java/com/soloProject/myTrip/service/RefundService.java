package com.soloProject.myTrip.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soloProject.myTrip.constant.*;
import com.soloProject.myTrip.dto.IamportResponse;
import com.soloProject.myTrip.dto.KakaoCancelResponse;
import com.soloProject.myTrip.dto.RefundRequestDto;
import com.soloProject.myTrip.entity.*;
import com.soloProject.myTrip.repository.*;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final RestTemplate restTemplate;
    private final ItemReservationRepository itemReservationRepository;
    private final ParticipantRepository participantRepository;
    private final CouponRepository couponRepository;
    private final MemberReservationRepository memberReservationRepository;
    private final ItemRepository itemRepository;

    // 동기화를 위한 락 객체
    private final Object refundLock = new Object();

    // 카카오페이 결제 취소
    public KakaoCancelResponse kakaoCancel(RefundRequestDto refundRequestDto) {
        synchronized (refundLock) {
            log.info("=== 카카오페이 결제 취소 시작 ===");
            log.info("취소 요청 정보 - paymentId: {}, amount: {}, flag: {}",
                    refundRequestDto.getPaymentId(),
                    refundRequestDto.getAmount(),
                    refundRequestDto.isFlag());

            Payment payment = paymentRepository.findById(refundRequestDto.getPaymentId())
                    .orElseThrow(EntityNotFoundException::new);

            // 결제 취소 처리
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.add("cid", "TC0ONETIME");
            parameters.add("tid", payment.getPaymentKey());
            parameters.add("cancel_amount", String.valueOf(refundRequestDto.getAmount()));
            parameters.add("cancel_tax_free_amount", "0");
            parameters.add("cancel_vat_amount", "0");

            log.debug("카카오페이 취소 요청 파라미터: {}", parameters);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters,
                    paymentService.getHeaders());

            try {
                log.info("카카오페이 취소 API 호출 시작");
                KakaoCancelResponse response = restTemplate.postForObject(
                        "https://kapi.kakao.com/v1/payment/cancel",
                        requestEntity,
                        KakaoCancelResponse.class);
                log.info("카카오페이 취소 API 호출 완료 - 응답: {}", response);

                // 결제 취소 후 데이터 업데이트
                if (refundRequestDto.isFlag()) {
                    log.info("예약금 결제 취소 처리 시작");
                    cancelPayment(payment, refundRequestDto);
                    log.info("예약금 결제 취소 처리 완료");
                } else {
                    log.info("잔금 결제 취소 처리 시작");
                    cancelPayment(payment, refundRequestDto);
                    log.info("잔금 결제 취소 처리 완료");
                }

                return response;
            } catch (Exception e) {
                log.error("카카오페이 취소 처리 중 오류 발생", e);
                log.error("에러 상세 - message: {}, cause: {}", e.getMessage(), e.getCause());
                throw e;
            }
        }
    }

    // 포트원 결제 취소
    public IamportResponse cancelIamportPayment(RefundRequestDto refundRequestDto) throws JsonProcessingException {
        try {
            log.info("포트원 결제 취소 시작 - paymentId: {}", refundRequestDto.getPaymentId());

            Payment payment = paymentRepository.findById(refundRequestDto.getPaymentId())
                    .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다."));

            // imp_uid와 merchant_uid를 DTO에서 가져옴
            String impUid = refundRequestDto.getImpUid();
            String merchantUid = refundRequestDto.getMerchantUid();

            log.info("결제 취소 요청 정보 - imp_uid: {}, amount: {}, merchant_uid: {}",
                    impUid, refundRequestDto.getAmount(), merchantUid);

            // 필수 값 검증 강화
            if (impUid == null || impUid.trim().isEmpty()) {
                String errorMsg = "imp_uid가 누락되었습니다.";
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            if (merchantUid == null || merchantUid.trim().isEmpty()) {
                String errorMsg = "merchant_uid가 누락되었습니다.";
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }

            // 결제 정보 로깅 추가
            log.info("결제 취소 요청 검증 완료 - imp_uid: {}, merchant_uid: {}", impUid, merchantUid);

            // 아임포트 API 토큰 발급
            String token = paymentService.getIamportToken();
            log.info("아임포트 토큰 발급 완료");

            // RestTemplate 요청 부분 수정
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // JSON 요청 바디 직접 구성
            String requestBody = String.format(
                    "{\"imp_uid\":\"%s\",\"merchant_uid\":\"%s\",\"amount\":%d,\"reason\":\"%s\"}",
                    refundRequestDto.getImpUid(),
                    refundRequestDto.getMerchantUid(),
                    refundRequestDto.getAmount(),
                    refundRequestDto.getReason());

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("실제 전송되는 요청 바디: {}", requestBody);

            ResponseEntity<IamportResponse> response = restTemplate.exchange(
                    "https://api.iamport.kr/payments/cancel",
                    org.springframework.http.HttpMethod.POST,
                    requestEntity,
                    IamportResponse.class);

            log.info("아임포트 API 응답 - Status: {}, Body: {}",
                    response.getStatusCode(),
                    new ObjectMapper().writeValueAsString(response.getBody()));

            if (response.getBody() == null) {
                log.error("아임포트 취소 응답이 null입니다.");
                throw new RuntimeException("결제 취소에 실패했습니다: 응답이 null입니다.");
            }

            if (response.getBody().getCode() != 0) {
                log.error("아임포트 취소 실패 - code: {}, message: {}",
                        response.getBody().getCode(),
                        response.getBody().getMessage());
                throw new RuntimeException("결제 취소에 실패했습니다: " + response.getBody().getMessage());
            }

            if (response.getBody().getResponse() == null) {
                log.error("아임포트 취소 응답의 response가 null입니다. 전체 응답: {}", response.getBody());
                throw new RuntimeException("결제 취소에 실패했습니다: 응답 데이터가 없습니다.");
            }

            // 잔금 결제 취소
            if (refundRequestDto.isFlag()) {
                cancelPayment(payment, refundRequestDto);
                return response.getBody();
            }
            // 예약금 결제 취소
            else {
                cancelPayment(payment, refundRequestDto);
                log.info("포트원 결제 취소 완료 - impUid: {}", impUid);
                return response.getBody();
            }

        } catch (Exception e) {
            log.error("포트원 결제 취소 실패 - 상세 에러: ", e);
            if (e instanceof RuntimeException) {
                throw e;
            }
            throw new RuntimeException("결제 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 결제 취소 시 데이터 업데이트 메서드
    private void cancelPayment(Payment payment, RefundRequestDto refundRequestDto) {
        log.info("=== 결제 취소 데이터 업데이트 시작 ===");
        Integer usedMileage = payment.getUsedMileage();
        MemberReservation reservation = payment.getMemberReservation();

        log.info("취소 처리할 예약 정보 - reservationId: {}, status: {}, usedMileage: {}",
                reservation.getId(),
                reservation.getReservationStatus(),
                usedMileage);

        // 예약금 취소일 때
        if (refundRequestDto.isFlag()) {
            ItemReservation itemReservation = itemReservationRepository
                    .findById(reservation.getItemReservation().getId())
                    .orElseThrow(EntityNotFoundException::new);

            Item item = itemRepository.findById(itemReservation.getItem().getId()).orElseThrow(EntityNotFoundException::new);

            reservation.setReservationStatus(ReservationStatus.REFUNDED);

            // ItemReservation 좌석 상태 업데이트
            List<Participant> adult = participantRepository.findByMemberReservationIdAndAge(reservation.getId(),
                    Age.ADULT);
            List<Participant> child = participantRepository.findByMemberReservationIdAndAge(reservation.getId(),
                    Age.CHILD);
            int cancelledSeat = adult.size() + child.size();
            int remainingSeats = itemReservation.getRemainingSeats() + cancelledSeat;
            itemReservation.updateRemainingSeats(remainingSeats);
            log.info("좌석 취소 처리 완료 - 취소 좌석 수: {}", cancelledSeat);

            // 취소 사유 등록
            payment.setRefundReason(refundRequestDto.getReason());
            log.info("취소 사유 등록 완료 - reason: {}", refundRequestDto.getReason());

            int paidParticipant = 0;
            for(MemberReservation memberReservation : itemReservation.getMemberReservations()){
                if(memberReservation.getReservationStatus().equals(ReservationStatus.DEPOSIT_PAID)){
                    List<Participant> adults = participantRepository.findByMemberReservationIdAndAge(memberReservation.getId(),Age.ADULT);
                    List<Participant> children = participantRepository.findByMemberReservationIdAndAge(memberReservation.getId(),Age.CHILD);
                    paidParticipant += (adults.size() + children.size());
                    log.info("예약금 지불 인원 : {}", paidParticipant);
                }
            }
            if(itemReservation.getItem().getMinParticipants() > paidParticipant){
                itemReservation.setDepartureConfirmed(false);
                log.info("예약 출발 확정 취소 - 예약 ID : {}, 예약 날짜 : {}",
                        itemReservation.getId(), itemReservation.getDepartureDateTime());

            }

            item.minusReservationCount();

            return;
        }

        // 잔금 취소일 때
        if (reservation.getReservationStatus().equals(ReservationStatus.BALANCE_PAID)) {
            log.info("잔금 결제 취소 관련 처리 시작");

            // 취소 사유 등록
            payment.setRefundReason(refundRequestDto.getReason());
            log.info("취소 사유 등록 완료 - reason: {}", refundRequestDto.getReason());

            // 잔금 결제에 사용된 쿠폰 및 마일리지 반환
            if (usedMileage > 0) {
                payment.getMemberReservation().getMember().cancelMileage(usedMileage);
                log.info("마일리지 반환 완료 - amount: {}", usedMileage);
            }

            if (payment.getUsedCoupon() != null) {
                Coupon usedCoupon = couponRepository.findById(payment.getUsedCoupon().getId())
                        .orElseThrow(EntityNotFoundException::new);
                usedCoupon.setCouponStatus(CouponStatus.USABLE);
                log.info("쿠폰 상태 변경 완료 - couponId: {}", usedCoupon.getId());
            }

            // 예약 상태를 DEPOSIT_PAID로 변경
            reservation.updateStatus(ReservationStatus.DEPOSIT_PAID);
            log.info("예약 상태 변경 완료 - status: {}", ReservationStatus.DEPOSIT_PAID);
        }
        log.info("=== 결제 취소 데이터 업데이트 완료 ===");
    }

    public List<Payment> getPayment(String reservationNumber) {
        MemberReservation reservation = memberReservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(EntityNotFoundException::new);

        List<Payment> payments = new ArrayList<>();

        // 예약금 결제 조회
        Payment depositPayment = paymentRepository.findByMemberReservationIdAndPaymentType(reservation.getId(),
                PaymentType.DEPOSIT);
        if (depositPayment != null) {
            payments.add(depositPayment);
        }

        // 잔금 결제 조회
        Payment balancePayment = paymentRepository.findByMemberReservationIdAndPaymentType(reservation.getId(),
                PaymentType.BALANCE);
        if (balancePayment != null) {
            payments.add(balancePayment);
        }

        return payments;
    }
}
