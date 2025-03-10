package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminStatisticsController {

  private final AdminStatisticsService adminStatisticsService;

  @GetMapping("/statistics")
  public String statistics(Model model) {
    return "admin/statistics";
  }

  @GetMapping("/api/statistics/reservations")
  @ResponseBody
  public Map<String, Object> getReservationStatistics() {
    return adminStatisticsService.getReservationStatistics();
  }

  @GetMapping("/api/statistics/sales")
  @ResponseBody
  public Map<String, Object> getSalesStatistics() {
    return adminStatisticsService.getSalesStatistics();
  }

  @GetMapping("/api/statistics/participants")
  @ResponseBody
  public Map<String, Object> getParticipantStatistics() {
    return adminStatisticsService.getParticipantStatistics();
  }
}