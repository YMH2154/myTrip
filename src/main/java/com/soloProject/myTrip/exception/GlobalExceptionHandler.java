package com.soloProject.myTrip.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

  @ExceptionHandler(AccessDeniedException.class)
  public String handleAccessDeniedException(AccessDeniedException e, Model model){
    model.addAttribute("errorMessage", e.getMessage());
    return "error/403";
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public String handleEntityNotFoundException(EntityNotFoundException e, Model model){
    model.addAttribute("errorMessage", e.getMessage());
    return "error/404";
  }
}
