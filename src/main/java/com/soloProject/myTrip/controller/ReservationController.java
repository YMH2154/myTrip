package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.constant.Age;
import com.soloProject.myTrip.constant.Sex;
import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.dto.ParticipantDto;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.exception.NotEnoughRemainingSeats;
import com.soloProject.myTrip.repository.ItemReservationRepository;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import com.soloProject.myTrip.service.ItemService;
import com.soloProject.myTrip.service.MemberService;
import com.soloProject.myTrip.service.ReservationService;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ItemReservationRepository itemReservationRepository;
    private final MemberReservationRepository memberReservationRepository;
    private final ItemService itemService;
    private final MemberService memberService;
    private final ReservationService reservationService;

    // 상품 예약 페이지
    @GetMapping("/reservation/new")
    public String newReservation(@RequestParam("itemId") Long itemId,
            @RequestParam("departureDateTime") String departureDateTime,
            @RequestParam("adultCount") int adultCount,
            @RequestParam("childCount") int childCount,
            @RequestParam("infantCount") int infantCount,
            Model model, Principal principal, HttpSession session) {

        // 예약 정보를 Map으로 저장
        Map<String, Object> reservationInfo = new HashMap<>();
        reservationInfo.put("itemId", itemId);
        reservationInfo.put("departureDateTime", departureDateTime);
        reservationInfo.put("adultCount", adultCount);
        reservationInfo.put("childCount", childCount);
        reservationInfo.put("infantCount", infantCount);

        // 비로그인 상태인 경우
        if (principal == null) {
            // 세션에 예약 정보와 이전 URL 저장
            session.setAttribute("reservationInfo", reservationInfo);
            session.setAttribute("prevPage", "/reservation/new");

            model.addAttribute("loginErrorMsg", "로그인이 필요한 서비스입니다.");
            return "redirect:/member/login";
        }

        try {
            ItemFormDto item = itemService.getItem(itemId);
            ItemReservation itemReservation = itemReservationRepository
                    .findByItemIdAndDepartureDateTime(itemId, departureDateTime);
            MemberFormDto member = memberService.getMemberDto(principal.getName());
            Map<String, Integer> peopleCount = new HashMap<>();
            peopleCount.put("adultCount", adultCount);
            peopleCount.put("childCount", childCount);
            peopleCount.put("infantCount", infantCount);

            model.addAttribute("item", item);
            model.addAttribute("itemReservation", itemReservation);
            model.addAttribute("peopleCount", peopleCount);
            model.addAttribute("member", member);

            return "reservation/reservationForm";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }

    //예약 데이터 저장 요청
    @PostMapping("/reservation/new")
    public String createReservation(@RequestParam("itemId") Long itemId,
            @RequestParam("departureDateTime") String departureDateTime,
            @RequestParam Map<String, String> paramMap,
            @RequestParam("totalDeposit") String totalDeposit,
            @RequestParam("totalPrice") String totalPrice,
            @RequestParam("bookerTel") String bookerTel,
            Principal principal,
                                    Model model) {
        try {
            // 쉼표 제거 후 정수로 변환
            int depositAmount = Integer.parseInt(totalDeposit.replaceAll("[^0-9]", ""));
            int priceAmount = Integer.parseInt(totalPrice.replaceAll("[^0-9]", ""));
            // 참가자 정보 파싱
            List<ParticipantDto> participants = new ArrayList<>();

            int index = 0;
            while (paramMap.containsKey("participants[" + index + "].name")) {
                ParticipantDto participant = new ParticipantDto();
                participant.setName(paramMap.get("participants[" + index + "].name"));
                participant.setBirth(paramMap.get("participants[" + index + "].birth"));
                participant.setTel(paramMap.get("participants[" + index + "].tel"));
                participant.setSex(Sex.valueOf(paramMap.get("participants[" + index + "].sex")));
                participant.setAge(Age.valueOf(paramMap.get("participants[" + index + "].age")));
                participants.add(participant);
                index++;
            }

            MemberReservation reservation = reservationService.createReservation(
                    itemId, departureDateTime, participants,
                    principal.getName(), depositAmount, priceAmount, bookerTel);

            return "redirect:/reservation/complete?reservationNumber=" +
                    reservation.getReservationNumber();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage",e.getMessage());
            return "redirect:/error";
        }
    }

    // 예약 성공 페이지
    @GetMapping("/reservation/complete")
    public String checkReservation(@RequestParam("reservationNumber") String reservationNumber,
            Model model) {
        MemberReservation reservation = memberReservationRepository
                .findByReservationNumber(reservationNumber).orElseThrow(EntityExistsException::new);

        model.addAttribute("reservation", reservation);
        return "reservation/complete";
    }

    //예약 취소 요청
    @GetMapping("/reservation/{reservationNumber}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelReservation(Principal principal,
                                               @PathVariable String reservationNumber) {
        try {
            reservationService.cancelReservation(reservationNumber, principal);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
}
