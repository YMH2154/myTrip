package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.config.SessionUtils;
import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.dto.ApproveResponse;
import com.soloProject.myTrip.dto.ErrorResponse;
import com.soloProject.myTrip.dto.PaymentRequestDto;
import com.soloProject.myTrip.dto.ReadyResponse;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import com.soloProject.myTrip.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentService paymentService;
  private final MemberReservationRepository memberReservationRepository;

  @GetMapping("/payment/ready/{reservationNumber}")
  public String paymentReady(@PathVariable String reservationNumber, Model model) {
    try {
      MemberReservation reservation = memberReservationRepository.findByReservationNumber(reservationNumber)
          .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

      model.addAttribute("reservation", reservation);
      model.addAttribute("paymentMethods", PaymentMethod.values());
      return "payment/paymentReady";
    } catch (Exception e) {
      e.printStackTrace();
      log.error("Payment preparation failed", e);
      return "redirect:/error";
    }
  }

  @PostMapping("/payment/prepare")
  public @ResponseBody ResponseEntity<?> preparePayment(@RequestBody PaymentRequestDto requestDto) {
    try {
      log.info("결제 준비 요청 - 예약번호: {}, 금액: {}", requestDto.getReservationNumber(), requestDto.getAmount());

      ReadyResponse readyResponse = paymentService.prepareKakaoPayment(requestDto);

      if (readyResponse != null && readyResponse.getTid() != null) {
        log.info("카카오페이 준비 성공 - TID: {}", readyResponse.getTid());
        SessionUtils.addAttribute("tid", readyResponse.getTid());
        return ResponseEntity.ok(readyResponse);
      } else {
        log.error("카카오페이 응답 없음");
        return ResponseEntity.badRequest().body(new ErrorResponse("카카오페이 응답을 받지 못했습니다."));
      }
    } catch (Exception e) {
      log.error("결제 준비 중 오류 발생", e);
      return ResponseEntity.badRequest().body(new ErrorResponse("결제 준비 중 오류가 발생했습니다: " + e.getMessage()));
    }
  }

  @GetMapping("/payment/success")
  public String paySuccess(@RequestParam("pg_token") String pgToken) {
    try {
      String tid = SessionUtils.getStringAttributeValue("tid");
      log.info("결제 승인 요청 - PG Token: {}, TID: {}", pgToken, tid);

      ApproveResponse approveResponse = paymentService.payApprove(tid, pgToken);
      log.info("결제 승인 성공 - 결제금액: {}", approveResponse.getAmount());

      return "redirect:/payment/completed";
    } catch (Exception e) {
      log.error("결제 승인 중 오류 발생", e);
      return "redirect:/payment/fail";
    }
  }

  @GetMapping("/payment/completed")
  public String paymentComplete(){
    log.info("결제 성공 페이지 호출");
    return "payment/success";
  }

  @GetMapping("/payment/fail")
  public String paymentFail() {
    log.info("결제 실패 페이지 호출");
    return "payment/fail";
  }

  @GetMapping("/payment/cancel")
  public String paymentCancel() {
    log.info("결제 취소 페이지 호출");
    return "payment/cancel";
  }
}
