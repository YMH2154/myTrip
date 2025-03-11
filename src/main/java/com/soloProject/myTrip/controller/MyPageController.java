package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.dto.QnADto;
import com.soloProject.myTrip.dto.MemberTelUpdateDto;
import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.service.CouponService;
import com.soloProject.myTrip.service.MemberService;
import com.soloProject.myTrip.service.QnAService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/myPage")
@RequiredArgsConstructor
public class MyPageController {

    private final QnAService qnAService;
    private final MemberService memberService;
    private final CouponService couponService;

    @GetMapping({ "", "/" })
    public String myPage() {
        return "myPage/myPage";
    }

    @GetMapping("/myInfo")
    public String myInfo(Principal principal, Model model) {
        String email = principal.getName();
        Member member = memberService.getMember(email);
        List<Coupon> coupons = couponService.getCouponsByMember(email);

        MemberTelUpdateDto memberTelUpdateDto = new MemberTelUpdateDto();
        memberTelUpdateDto.setTel(member.getTel());

        model.addAttribute("member", member);
        model.addAttribute("memberTelUpdateDto", memberTelUpdateDto);
        model.addAttribute("coupons", coupons);

        return "myPage/myInfo";
    }

    @GetMapping("/recent-items")
    public String recentItems() {
        return "myPage/recentItems";
    }

    @GetMapping("/qna")
    public String qna(Model model, Principal principal) {
        try {
            List<QnADto> qnaDtos = qnAService.getMemberQnAPage(principal.getName());
            model.addAttribute("qnaDtos", qnaDtos);
            return "myPage/qna";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }

    @GetMapping("/payments")
    public String payments() {
        return "myPage/payments";
    }

    @GetMapping("/points")
    public String points() {
        return "myPage/points";
    }

}
