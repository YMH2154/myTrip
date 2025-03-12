package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.QnADto;
import com.soloProject.myTrip.dto.MemberTelUpdateDto;
import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.Payment;
import com.soloProject.myTrip.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/myPage")
@RequiredArgsConstructor
public class MyPageController {

    private final QnAService qnAService;
    private final MemberService memberService;
    private final CouponService couponService;
    private final RecentViewService recentViewService;
    private final PaymentService paymentService;

    @GetMapping({ "", "/" })
    public String myPage(Principal principal, HttpServletRequest request, Model model) {
        try {
            String email = principal.getName();
            Member member = memberService.getMember(email);
            List<Coupon> coupons = couponService.getCouponsByMember(email);

            // 기존 데이터
            List<Item> recentItems = recentViewService.getRecentItems(request, null);
            List<QnADto> recentQnAs = qnAService.getMemberQnAPage(email)
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());
            Page<Payment> recentPayments = paymentService.getPaymentsByEmail(
                    email,
                    PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "regTime")));

            // 모델에 데이터 추가
            model.addAttribute("member", member);
            model.addAttribute("coupons", coupons);
            model.addAttribute("recentItems", recentItems);
            model.addAttribute("recentQnAs", recentQnAs);
            model.addAttribute("recentPayments", recentPayments.getContent());

            return "myPage/myPage";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
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
    public String recentItems(HttpServletRequest request, Model model) {
        // 현재 보고 있는 상품이 없으므로 null 전달
        List<Item> recentItems = recentViewService.getRecentItems(request, null);
        model.addAttribute("recentItems", recentItems);
        return "myPage/recent-items";
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
    public String payments(Principal principal, Model model,
            @PageableDefault(size = 10, sort = "regTime", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<Payment> paymentPage = paymentService.getPaymentsByEmail(principal.getName(), pageable);
            model.addAttribute("payments", paymentPage);
            model.addAttribute("maxPage", 5); // 페이징 네비게이션에 보여줄 최대 페이지 수
            return "myPage/payments";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }

}
