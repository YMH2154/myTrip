package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import com.soloProject.myTrip.service.MemberService;
import com.soloProject.myTrip.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final MemberService memberService;
    private final MemberReservationRepository memberReservationRepository;

    @GetMapping("/{memberReservationNumber}")
    public String goToPaymentPage(@PathVariable("memberReservationNumber") String memberReservationNumber,
                                Model model, Principal principal) {
        try {
            MemberFormDto member = memberService.getMember(principal.getName());
            MemberReservation reservation = memberReservationRepository
                .findByReservationNumber(memberReservationNumber)
                .orElseThrow(EntityNotFoundException::new);
            
            model.addAttribute("reservation", reservation);
            model.addAttribute("member", member);
            return "payment/paymentReady";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }

    @PostMapping("/kakao/ready")
    @ResponseBody
    public ResponseEntity<String> kakaoPayReady(@RequestParam("reservationNumber") String reservationNumber) {
        try {
            MemberReservation reservation = memberReservationRepository
                .findByReservationNumber(reservationNumber)
                .orElseThrow(EntityNotFoundException::new);
            
            String response = paymentService.prepareKakaoPayment(reservation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/naver/ready")
    @ResponseBody
    public ResponseEntity<String> naverPayReady(@RequestParam("reservationNumber") String reservationNumber) {
        try {
            MemberReservation reservation = memberReservationRepository
                .findByReservationNumber(reservationNumber)
                .orElseThrow(EntityNotFoundException::new);
            
            String response = paymentService.prepareNaverPayment(reservation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/toss/ready")
    @ResponseBody
    public ResponseEntity<String> tossPayReady(@RequestParam("reservationNumber") String reservationNumber) {
        try {
            MemberReservation reservation = memberReservationRepository
                .findByReservationNumber(reservationNumber)
                .orElseThrow(EntityNotFoundException::new);
            
            String response = paymentService.prepareTossPayment(reservation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
