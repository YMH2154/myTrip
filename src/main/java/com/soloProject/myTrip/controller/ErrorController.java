package com.soloProject.myTrip.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(Exception.class)
    public String handleException(HttpServletRequest request, Exception ex, Model model) {
        model.addAttribute("url", request.getRequestURI());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "error/error";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(HttpServletRequest request, Model model) {
        model.addAttribute("url", request.getRequestURI());
        model.addAttribute("errorMessage", "요청한 페이지를 찾을 수 없습니다.");
        model.addAttribute("status", HttpStatus.NOT_FOUND.value());
        return "error/error";
    }

    @ExceptionHandler({ CsrfException.class, MissingCsrfTokenException.class, InvalidCsrfTokenException.class })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleCsrfException(HttpServletRequest request, Exception ex, Model model) {
        model.addAttribute("url", request.getRequestURI());
        model.addAttribute("errorMessage", "보안 검증에 실패했습니다. 페이지를 새로고침하고 다시 시도해주세요.");
        model.addAttribute("status", HttpStatus.FORBIDDEN.value());
        return "error/error";
    }
}