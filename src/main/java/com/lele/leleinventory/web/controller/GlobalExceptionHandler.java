package com.lele.leleinventory.web.controller;

import com.lele.leleinventory.service.BusinessValidationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessValidationException.class)
  public String handleBusiness(BusinessValidationException ex, Model model) {
    model.addAttribute("errorMessage", ex.getMessage());
    return "error";
  }
}

