package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.annotaion.CheckQnAAccess;
import com.soloProject.myTrip.dto.QnADto;
import com.soloProject.myTrip.service.QnAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class QnAController {
    private final QnAService qnAService;

    @ModelAttribute("unansweredCount")
    public Long getUnansweredCount() {
        return qnAService.getUnansweredCount();
    }

    // qna 등록 페이지
    @GetMapping("/myPage/qna/new")
    public String qnAFormPage(Model model) {
        model.addAttribute("qnaDto", new QnADto());
        return "qna/qnaForm";
    }

    // qna 저장 요청
    @PostMapping("/myPage/qna/new")
    public String newQnA(@Valid QnADto qnADto,
            Principal principal) {
        try {
            qnAService.saveQuestion(qnADto, principal.getName());
            return "redirect:/myPage/qna";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }

    // qna 상세 페이지
    @CheckQnAAccess
    @GetMapping("/myPage/qna/{qnaId}")
    public String qnADtlPage(Model model,
            @PathVariable("qnaId") Long qnaId,
            Principal principal) {
        QnADto qnaDto = qnAService.getQnADtl(qnaId, principal.getName());
        model.addAttribute("qnaDto", qnaDto);
        return "qna/qnaRead";
    }

    // qna 수정 페이지
    @CheckQnAAccess
    @GetMapping("/myPage/qna/{qnaId}/edit")
    public String qnaEditPage(Model model,
            @PathVariable("qnaId") Long qnaId) {
        try {
            QnADto qnaDto = qnAService.getQnADtl(qnaId);
            model.addAttribute("qnaDto", qnaDto);
            return "qna/qnaEdit";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }

    // qna 수정 요청
    @CheckQnAAccess
    @PutMapping("/myPage/qna/{qnaId}/edit")
    @ResponseBody
    public ResponseEntity<?> editQnA(@PathVariable("qnaId") Long qnaId,
            @RequestBody QnADto qnaDto) {
        try {
            qnaDto.setId(qnaId); // PathVariable의 id를 DTO에 설정
            qnAService.updateQnA(qnaDto);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("에러 발생");
        }
    }

    // qna 삭제 요청
    @CheckQnAAccess
    @DeleteMapping("/myPage/qna/{qnaId}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteQnA(@PathVariable("qnaId") Long qnaId) {
        try {
            qnAService.deleteQuestion(qnaId);
            return new ResponseEntity<>("삭제 완료", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("삭제 실패", HttpStatus.BAD_REQUEST);
        }
    }

    // 답변 페이지
    @GetMapping("/admin/qna/{qnaId}/answer")
    public String answerPage(@PathVariable("qnaId") Long qnaId,
            Model model) {
        try {
            QnADto qnaDto = qnAService.getQnADtl(qnaId);
            model.addAttribute("qnaDto", qnaDto);
            return "qna/qnaAnswer";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }

    // 답변 등록
    @PostMapping("/admin/qna/{qnaId}/answer")
    public String submitAnswer(@PathVariable("qnaId") Long qnaId,
            @RequestParam String answer) {
        try {
            qnAService.saveAnswer(qnaId, answer);
            return "redirect:/admin/qnas";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }
}
