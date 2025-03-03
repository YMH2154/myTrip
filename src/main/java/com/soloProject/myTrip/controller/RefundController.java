package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.dto.*;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.entity.Payment;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import com.soloProject.myTrip.repository.PaymentRepository;
import com.soloProject.myTrip.service.RefundService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final MemberReservationRepository memberReservationRepository;
    private final PaymentRepository paymentRepository;

    // 환불(결제 취소)
    @GetMapping("/refund/{reservationNumber}")
    public String paymentRefund(Model model, @PathVariable("reservationNumber") String reservationNumber,
            Principal principal) {
        try {
            MemberReservation reservation = memberReservationRepository.findByReservationNumber(reservationNumber)
                    .orElseThrow(
                            () -> new EntityNotFoundException("예약번호 " + reservationNumber + "에 해당하는 예약을 찾을 수 없습니다."));
            List<Payment> payments = refundService.getPayment(reservationNumber);

            if (reservation == null) {
                log.error("예약을 찾을 수 없습니다. 예약번호: {}", reservationNumber);
                model.addAttribute("errorMessage", "예약 정보를 찾을 수 없습니다.");
                return "error/error";
            }

            if (!reservation.getMember().getEmail().equals(principal.getName())) {
                log.error("잘못된 사용자 접근. 예약번호: {}, 접근 사용자: {}", reservationNumber, principal.getName());
                model.addAttribute("errorMessage", "올바르지 않은 사용자입니다.");
                return "error/error";
            }

            // 필요한 데이터만 DTO로 변환
            List<PaymentDto> paymentDtos = PaymentDto.createDtoList(payments);
            RefundInfoDto refundInfo = RefundInfoDto.createDto(reservation, paymentDtos);

            model.addAttribute("refundInfo", refundInfo);
            return "refund/refund";

        } catch (EntityNotFoundException e) {
            log.error("환불 페이지 호출 중 예약을 찾을 수 없음. 예약번호: {}", reservationNumber, e);
            model.addAttribute("errorMessage", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            log.error("환불 페이지 호출 중 오류 발생. 예약번호: {}", reservationNumber, e);
            model.addAttribute("errorMessage", "환불 처리 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @PostMapping("/refund/prepare")
    public @ResponseBody ResponseEntity<?> refund(@RequestBody RefundRequestDto refundRequestDto) {
        log.info("controller refund - paymentId: {}, amount: {}",
                refundRequestDto.getPaymentId(), refundRequestDto.getAmount());

        try {
            Payment payment = paymentRepository.findById(refundRequestDto.getPaymentId())
                    .orElseThrow(EntityNotFoundException::new);

            if (payment.getPaymentMethod() == PaymentMethod.KAKAO) {
                KakaoCancelResponse kakaoCancelResponse = refundService.kakaoCancel(refundRequestDto);
                return new ResponseEntity<>(kakaoCancelResponse, HttpStatus.OK);
            } else if (payment.getPaymentMethod() == PaymentMethod.CARD) {
                IamportResponse iamportResponse = refundService.cancelIamportPayment(refundRequestDto);
                return new ResponseEntity<>(iamportResponse, HttpStatus.OK);
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse("지원하지 않는 결제 방식입니다."));
            }
        } catch (Exception e) {
            log.error("결제 취소 실패", e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/refund/success")
    public String refundSuccess() {
        log.info("환불 성공 페이지 호출");
        return "refund/success";
    }

}
