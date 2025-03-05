package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.QnADto;
import com.soloProject.myTrip.service.QnAService;
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

    @GetMapping({ "", "/" })
    public String myPage() {
        return "myPage/myPage";
    }

    @GetMapping("/info")
    public String myInfo() {
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
