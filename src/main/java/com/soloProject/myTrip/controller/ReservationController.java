package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.repository.ItemReservationRepository;
import com.soloProject.myTrip.service.ItemService;
import com.soloProject.myTrip.service.MemberService;
import com.soloProject.myTrip.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ItemReservationRepository itemReservationRepository;
    private final ItemService itemService;
    private final MemberService memberService;
    private final ReservationService reservationService;

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
            MemberFormDto member = memberService.getMember(principal.getName());
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

    @PostMapping("/reservation/new")
    public String createReservation(@RequestParam("itemId") Long itemId,
                                  @RequestParam("departureDateTime") String departureDateTime,
                                  @RequestParam("names") List<String> names,
                                  @RequestParam("births") List<String> births,
                                  @RequestParam("sexes") List<String> sexes,
                                  Principal principal) {
        try {
            reservationService.createReservation(itemId, departureDateTime, names, births, sexes, principal.getName());
            return "redirect:/reservation/complete"; // 예약 확인 페이지로 리다이렉트
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    //예약 확인 페이지
    @GetMapping("/reservation/complete")
    public String checkReservation(){
        return "reservation/complete";
    }
}
