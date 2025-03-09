package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.MemberFormDto;
import com.soloProject.myTrip.dto.MemberUpdateFormDto;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.repository.MemberRepository;
import com.soloProject.myTrip.service.EmailService;
import com.soloProject.myTrip.service.MemberService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final MemberRepository memberRepository;
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

    // 예약 내역 조회
    @GetMapping("/reservations")
    public String reservationList(Principal principal, Model model) {
        try {
            List<MemberReservation> reservations = memberService.getReservations(principal.getName());
            model.addAttribute("reservations", reservations);
            return "member/history";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    @GetMapping("/reservations/{reservationNumber}")
    public String reservationDetail(@PathVariable("reservationNumber") String reservationNumber, Model model, Principal principal) {

        try {
            MemberReservation reservation = memberService.getReservationByNumber(reservationNumber);
            if(!Objects.equals(reservation.getMember().getEmail(), principal.getName())){
                model.addAttribute("errorMessage","접근 권한이 없습니다.");
                    return "member/history";
            }
            model.addAttribute("reservation", reservation);
            return "reservation/reservationDetail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    //미구현
    //여권 정보 등록
    @GetMapping("/reservations/passport/{reservationNumber}")
    public String passportRegist(@PathVariable("reservationNumber")String reservationNumber, Model model, Principal principal){
        try {
            MemberReservation reservation = memberService.getReservationByNumber(reservationNumber);
            if(!Objects.equals(reservation.getMember().getEmail(), principal.getName())){
                model.addAttribute("errorMessage","접근 권한이 없습니다.");
                return "member/history";
            }
            model.addAttribute("reservation", reservation);
            return "reservation/passport";
        } catch (Exception e){
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    @GetMapping("/myPage")
    public String memberMyPage(Model model, Principal principal) {
        String email = principal.getName();

        if (email == null) {
            throw new IllegalStateException("로그인된 사용자의 이메일 정보를 가져올 수 없습니다.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        System.out.println("Member found: " + member);

        MemberUpdateFormDto memberUpdateFormDto = new MemberUpdateFormDto();
        memberUpdateFormDto.setTel(member.getTel());

        model.addAttribute("member", member);
        model.addAttribute("memberUpdateFormDto", memberUpdateFormDto);

        return "member/myPage";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("errorMessage", "로그인 정보가 없습니다. 다시 로그인해주세요.");
            reloadMemberData(model, principal);
            return "member/myPage"; // 전체 페이지 반환
        }

        // 입력값 검증
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {
            model.addAttribute("errorMessage", "모든 빈칸을 입력해주세요.");
            reloadMemberData(model, principal);
            return "member/myPage"; // 전체 페이지 반환
        }

        String email = principal.getName();

        try {
            memberService.changePassword(email, currentPassword, newPassword, confirmPassword);
            model.addAttribute("successMessage", "비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        // 필요한 데이터를 다시 로드
        reloadMemberData(model, principal);

        return "member/myPage"; // 전체 페이지 반환
    }

    private void reloadMemberData(Model model, Principal principal) {
        String email = principal.getName();

        if (email == null) {
            throw new IllegalStateException("로그인된 사용자의 이메일 정보를 가져올 수 없습니다.");
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        MemberUpdateFormDto memberUpdateFormDto = new MemberUpdateFormDto();
        memberUpdateFormDto.setTel(member.getTel());

        model.addAttribute("member", member);
        model.addAttribute("memberUpdateFormDto", memberUpdateFormDto);
    }

    @PostMapping(value = "/update/{id}")
    public String memberUpdate(@PathVariable Long id,
                               @Valid MemberUpdateFormDto memberUpdateFormDto,
                               BindingResult bindingResult,
                               Model model,
                               Principal principal) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "입력값에 오류가 있습니다. 다시 확인해주세요.");
            reloadMemberData(model, principal);
            return "myPage/myInfo";
        }

        try {
            memberService.updateMember(memberUpdateFormDto, id);
            model.addAttribute("successMessage", "정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "정보 수정 중 에러가 발생하였습니다.");
        }

        reloadMemberData(model, principal);
        return "redirect:/myPage/info";
    }
}
