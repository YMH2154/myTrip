package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.config.SessionUtils;
import com.soloProject.myTrip.constant.CouponStatus;
import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.dto.*;
import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.entity.CouponWallet;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.entity.Payment;
import com.soloProject.myTrip.repository.*;
import com.soloProject.myTrip.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

  private final PaymentService paymentService;
  private final MemberReservationRepository memberReservationRepository;
  private final PaymentRepository paymentRepository;
  private final CouponRepository couponRepository;
  private final MemberRepository memberRepository;
  private final CouponWalletRepository couponWalletRepository;

  @GetMapping("/payment/ready/{reservationNumber}")
  public String paymentReady(@PathVariable String reservationNumber, Model model, Principal principal) {
    try {
      MemberReservation reservation = memberReservationRepository.findByReservationNumber(reservationNumber)
          .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다."));

      CouponWallet couponWallet = couponWalletRepository.findByMemberId(
              memberRepository.findByEmail(principal.getName())
                      .orElseThrow(EntityNotFoundException::new)
                      .getId()
      );

      List<Coupon> coupons = couponRepository.findByCouponWalletIdAndCouponStatus(
              couponWallet.getId(),
              CouponStatus.USABLE
      );
      log.info("쿠폰 지갑 아이디 : {}", couponWallet.getId());
      log.info("쿠폰 갯수 : {}", coupons.size());

      int memberMilegae =  memberRepository.findByEmail(principal.getName())
              .orElseThrow(EntityNotFoundException::new)
              .getMileage();

      model.addAttribute("availableCoupons", coupons);
      model.addAttribute("memberMileage", memberMilegae);
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
  public String paymentComplete() {
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

  // 환불(결제 취소)
  @GetMapping("/payment/refund/{reservationNumber}")
  public String paymentRefund(Model model, @PathVariable("reservationNumber") String reservationNumber,
      Principal principal) {
    try {
      MemberReservation reservation = memberReservationRepository.findByReservationNumber(reservationNumber)
          .orElseThrow(() -> new EntityNotFoundException("예약번호 " + reservationNumber + "에 해당하는 예약을 찾을 수 없습니다."));
      Payment payment = paymentService.getPayment(reservationNumber);

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

      int amount = reservation.getReservationStatus().equals(ReservationStatus.DEPOSIT_PAID)
          ? reservation.getTotalDeposit()
          : reservation.getTotalPrice();

      log.info("환불 페이지 호출 - 예약번호: {}, 금액: {}", reservationNumber, amount);

      model.addAttribute("payment", payment);
      model.addAttribute("amount", amount);
      return "payment/refund/refund";

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

  @PostMapping("/payment/refund/prepare")
  public @ResponseBody ResponseEntity<?> refund(@RequestBody RefundRequestDto refundRequestDto) {
    log.info("controller refund - paymentId: {}, amount: {}",
        refundRequestDto.getPaymentId(), refundRequestDto.getAmount());

    try {
      Payment payment = paymentRepository.findById(refundRequestDto.getPaymentId()).orElseThrow(EntityNotFoundException::new);

      if (payment.getPaymentMethod() == PaymentMethod.KAKAO) {
        KakaoCancelResponse kakaoCancelResponse = paymentService.kakaoCancel(refundRequestDto);
        return new ResponseEntity<>(kakaoCancelResponse, HttpStatus.OK);
      } else if (payment.getPaymentMethod() == PaymentMethod.CARD) {
        IamportResponse iamportResponse = paymentService.cancelIamportPayment(refundRequestDto);
        return new ResponseEntity<>(iamportResponse, HttpStatus.OK);
      } else {
        return ResponseEntity.badRequest().body(new ErrorResponse("지원하지 않는 결제 방식입니다."));
      }
    } catch (Exception e) {
      log.error("결제 취소 실패", e);
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
  }

  @GetMapping("/payment/refund/success")
  public String refundSuccess() {
    log.info("환불 성공 페이지 호출");
    return "payment/refund/success";
  }

  @PostMapping("/payment/card/prepare")
  public @ResponseBody ResponseEntity<?> prepareCardPayment(@RequestBody PaymentRequestDto requestDto,
      Principal principal) {
    log.info("카드결제 준비 요청 - 예약번호: {}, 금액: {}", requestDto.getReservationNumber(), requestDto.getAmount());
    try {
      CardPaymentPrepareResponse response = paymentService.prepareCardPayment(requestDto, principal.getName());
      log.info("카드결제 준비 성공 - merchantUid: {}", response.getMerchantUid());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("카드결제 준비 실패", e);
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
  }

  @PostMapping("/payment/card/verify")
  public @ResponseBody ResponseEntity<?> verifyCardPayment(@RequestBody CardPaymentVerifyRequest request) {
    log.info("카드결제 검증 요청 - impUid: {}, merchantUid: {}", request.getImpUid(), request.getMerchantUid());
    try {
      CardPaymentVerifyResponse response = paymentService.verifyCardPayment(request);
      log.info("카드결제 검증 성공 - 결제금액: {}", response.getAmount());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("카드결제 검증 실패", e);
      return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
  }
}
