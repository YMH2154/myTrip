package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import com.soloProject.myTrip.service.PaymentService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.extern.slf4j.Slf4j;
import com.soloProject.myTrip.dto.PaymentRequestDto;
import com.soloProject.myTrip.dto.ErrorResponse;
import com.soloProject.myTrip.dto.PaymentResponseDto;

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
      log.error("Payment preparation failed", e);
      return "redirect:/error";
    }
  }

  @PostMapping("/payment/prepare")
  @ResponseBody
  public ResponseEntity<?> preparePayment(@RequestBody PaymentRequestDto requestDto) {
    try {
      log.info("Payment preparation requested for method: {}", requestDto.getPaymentMethod());
      PaymentResponseDto response = paymentService.preparePayment(requestDto);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Payment preparation failed", e);
      return ResponseEntity.badRequest()
          .body(new ErrorResponse("결제 준비 중 오류가 발생했습니다: " + e.getMessage()));
    }
  }

  @GetMapping("/payment/success")
  public String paymentSuccess(@RequestParam String paymentKey,
      @RequestParam String orderId,
      @RequestParam Integer amount,
      Model model) {
    try {
      PaymentResponseDto response = paymentService.completePayment(paymentKey, orderId, amount);
      model.addAttribute("payment", response);
      return "payment/success";
    } catch (Exception e) {
      log.error("Payment completion failed", e);
      return "redirect:/payment/fail";
    }
  }
}
