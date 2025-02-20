package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.service.EmailService;
import com.soloProject.myTrip.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private Boolean confirmCheck = false;

    // 회원가입(GET)
    @GetMapping("/new")
    public String newMember(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/newAccount";
    }

    // 회원가입(POST)
    @PostMapping("/new")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult,  Model model) {
        if (bindingResult.hasErrors()) {
            return "member/newAccount";
        }
        if (!confirmCheck) {
            model.addAttribute("errorMessage", "이메일 인증이 필요합니다");
            return "member/newAccount";
        }
        if (!memberFormDto.getPassword().equals(memberFormDto.getCheckPassword())) {
            model.addAttribute("errorMessage", "비밀번호 확인이 일치하지 않습니다");
            return "member/newAccount";
        }
        if (!memberFormDto.getInputCode().equals(emailService.getAuthCode())){
            model.addAttribute("errorMessage", "인증 코드가 일치하지 않습니다.");
            return "member/newAccount";
        }
        try {
            memberService.saveMember(memberFormDto);
            return "member/loginForm";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/newAccount";
        }
    }

    // 로그인(GET)
    @GetMapping("/login")
    public String loginForm(Model model, HttpSession session) {
        // 세션에서 예약 정보가 있는 경우 로그인 필요 메시지 표시
        if (session.getAttribute("reservationInfo") != null) {
            model.addAttribute("loginErrorMsg", "로그인이 필요한 서비스입니다.");
        }
        return "member/loginForm";
    }

    // 로그인 에러(GET)
    @GetMapping("/login/error")
    public String loginError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요.");
        return "member/loginForm";
    }

    // 인증 코드 전송(POST)
    @PostMapping("/email/sendCode")
    @ResponseBody
    public ResponseEntity<String> emailSend(@RequestBody Map<String, String> emailMap) {
        try {
            String email = emailMap.get("email");
            emailService.sendEmail(email);
            return ResponseEntity.ok("이메일이 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이메일 발송에 실패했습니다: " + e.getMessage());
        }
    }

    // 인증 코드 확인(POST)
    @PostMapping("/email/inputCode")
    @ResponseBody
    public ResponseEntity<String> checkCode(@RequestBody Map<String, String> request) {
        String inputCode = request.get("inputCode");
        if (emailService.getAuthCode().equals(inputCode)) {
            confirmCheck = true;
            return new ResponseEntity<>("인증이 완료되었습니다", HttpStatus.OK);
        }
        return new ResponseEntity<>("인증코드가 일치하지 않습니다", HttpStatus.BAD_REQUEST);
    }

    // 로그인 성공 처리
    @GetMapping("/login/success")
    public String loginSuccess(HttpSession session) {
        // 세션에서 예약 정보 확인
        Map<String, Object> reservationInfo = (Map<String, Object>) session.getAttribute("reservationInfo");
        String prevPage = (String) session.getAttribute("prevPage");
        
        // 세션 데이터 삭제
        session.removeAttribute("reservationInfo");
        session.removeAttribute("prevPage");
        
        // 이전 페이지가 예약 페이지였다면 예약 정보와 함께 리다이렉트
        if (reservationInfo != null && "/reservation/new".equals(prevPage)) {
            return "redirect:/reservation/new" +
                    "?itemId=" + reservationInfo.get("itemId") +
                    "&departureDateTime=" + reservationInfo.get("departureDateTime") +
                    "&adultCount=" + reservationInfo.get("adultCount") +
                    "&childCount=" + reservationInfo.get("childCount") +
                    "&infantCount=" + reservationInfo.get("infantCount");
        }
        
        return "redirect:/";
    }
}
