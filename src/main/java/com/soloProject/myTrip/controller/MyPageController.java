package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.dto.MemberUpdateFormDto;
import com.soloProject.myTrip.dto.QnADto;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.service.MemberService;
import com.soloProject.myTrip.service.QnAService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final QnAService qnAService;
    private final MemberService memberService;

    @GetMapping({ "", "/" })
    public String myPage() {
        return "myPage/myPage";
    }

    @GetMapping("/info")
    public String myInfo(Principal principal, Model model) {
        String email = principal.getName();

        if (email == null) {
            throw new IllegalStateException("로그인된 사용자의 이메일 정보를 가져올 수 없습니다.");
        }

        Member member = memberService.getMember(principal.getName());

        System.out.println("Member found: " + member.getId());

        MemberUpdateFormDto memberUpdateFormDto = new MemberUpdateFormDto();
        memberUpdateFormDto.setTel(member.getTel());

        model.addAttribute("member", member);
        model.addAttribute("memberUpdateFormDto", memberUpdateFormDto);
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
