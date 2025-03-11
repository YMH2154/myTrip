package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class StatisticsController {

  private final StatisticsService statisticsService;

  @GetMapping("/api/statistics/reservations")
  @ResponseBody
  public Map<String, Object> getReservationStatistics(
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate,
      @RequestParam(defaultValue = "month") String timeUnit) {

    log.info("예약 통계 요청 - 시작일: {}, 종료일: {}, 시간단위: {}", startDate, endDate, timeUnit);

    try {
      if (startDate == null) {
        startDate = LocalDateTime.now().minusMonths(6);
      }
      if (endDate == null) {
        endDate = LocalDateTime.now();
      }

      Map<String, Object> result = statisticsService.getReservationStatistics(startDate, endDate, timeUnit);
      log.info("예약 통계 조회 성공 - 데이터 크기: {}", result.size());
      return result;

    } catch (Exception e) {
      log.error("예약 통계 조회 실패", e);
      return Map.of("error", "통계 데이터 조회 중 오류가 발생했습니다.");
    }
  }

  @GetMapping("/api/statistics/sales")
  @ResponseBody
  public Map<String, Object> getSalesStatistics(
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate,
      @RequestParam(defaultValue = "month") String timeUnit) {

    log.info("매출 통계 요청 - 시작일: {}, 종료일: {}, 시간단위: {}", startDate, endDate, timeUnit);

    try {
      if (startDate == null) {
        startDate = LocalDateTime.now().minusMonths(6);
      }
      if (endDate == null) {
        endDate = LocalDateTime.now();
      }

      Map<String, Object> result = statisticsService.getSalesStatistics(startDate, endDate);
      log.info("매출 통계 조회 성공 - 데이터 크기: {}", result.size());
      return result;

    } catch (Exception e) {
      log.error("매출 통계 조회 실패", e);
      return Map.of("error", "통계 데이터 조회 중 오류가 발생했습니다.");
    }
  }

  @GetMapping("/api/statistics/participants")
  @ResponseBody
  public Map<String, Object> getParticipantStatistics(
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate) {

    log.info("참가자 통계 요청 - 시작일: {}, 종료일: {}", startDate, endDate);

    try {
      if (startDate == null) {
        startDate = LocalDateTime.now().minusMonths(6);
      }
      if (endDate == null) {
        endDate = LocalDateTime.now();
      }

      Map<String, Object> result = statisticsService.getParticipantStatistics(startDate, endDate);
      log.info("참가자 통계 조회 성공 - 데이터 크기: {}", result.size());
      return result;

    } catch (Exception e) {
      log.error("참가자 통계 조회 실패", e);
      return Map.of("error", "통계 데이터 조회 중 오류가 발생했습니다.");
    }
  }

  @GetMapping("/api/statistics/all")
  @ResponseBody
  public Map<String, Object> getAllStatistics(
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime endDate,
      @RequestParam(defaultValue = "month") String timeUnit) {

    log.info("전체 통계 요청 - 시작일: {}, 종료일: {}, 시간단위: {}", startDate, endDate, timeUnit);

    try {
      if (startDate == null) {
        startDate = LocalDateTime.now().minusMonths(6);
      }
      if (endDate == null) {
        endDate = LocalDateTime.now();
      }

      Map<String, Object> allStats = new HashMap<>();

      log.info("예약 통계 조회 시작");
      allStats.putAll(statisticsService.getReservationStatistics(startDate, endDate, timeUnit));

      log.info("매출 통계 조회 시작");
      allStats.putAll(statisticsService.getSalesStatistics(startDate, endDate));

      log.info("참가자 통계 조회 시작");
      allStats.putAll(statisticsService.getParticipantStatistics(startDate, endDate));

      log.info("전체 통계 조회 성공 - 데이터 크기: {}", allStats.size());
      return allStats;

    } catch (Exception e) {
      log.error("전체 통계 조회 실패", e);
      return Map.of("error", "통계 데이터 조회 중 오류가 발생했습니다.");
    }
  }
}