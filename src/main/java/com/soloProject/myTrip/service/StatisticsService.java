package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.Age;
import com.soloProject.myTrip.constant.PaymentMethod;
import com.soloProject.myTrip.constant.PaymentType;
import com.soloProject.myTrip.entity.*;
import com.soloProject.myTrip.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticsService {

  private final MemberReservationRepository memberReservationRepository;
  private final PaymentRepository paymentRepository;
  private final ParticipantRepository participantRepository;

  public Map<String, Object> getReservationStatistics(LocalDateTime startDate, LocalDateTime endDate, String timeUnit) {
    log.info("예약 통계 서비스 시작 - 시작일: {}, 종료일: {}, 시간단위: {}", startDate, endDate, timeUnit);

    try {
      Map<String, Object> result = new HashMap<>();

      String pattern = switch (timeUnit) {
        case "day" -> "%Y-%m-%d";
        case "week" -> "%Y-%u";
        case "month" -> "%Y-%m";
        default -> "%Y-%m";
      };

      List<Object[]> statistics = memberReservationRepository.getReservationStatistics(startDate, endDate, pattern);
      log.info("예약 통계 데이터 조회 완료 - 데이터 수: {}", statistics.size());

      Map<String, Long> groupedReservations = new LinkedHashMap<>();
      for (Object[] stat : statistics) {
        groupedReservations.put((String) stat[0], (Long) stat[1]);
      }

      result.put("reservations", groupedReservations);
      return result;

    } catch (Exception e) {
      log.error("예약 통계 처리 중 오류 발생", e);
      throw new RuntimeException("예약 통계 처리 실패", e);
    }
  }

  public Map<String, Object> getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
    log.info("매출 통계 서비스 시작 - 시작일: {}, 종료일: {}", startDate, endDate);

    try {
      Map<String, Object> result = new HashMap<>();

      // 매출 통계
      List<Object[]> salesStats = paymentRepository.getSalesStatistics(startDate, endDate, "%Y-%m");
      Map<String, Integer> monthlySales = new LinkedHashMap<>();
      for (Object[] stat : salesStats) {
        monthlySales.put((String) stat[0], ((Number) stat[1]).intValue());
      }
      result.put("monthlySales", monthlySales);

      log.info("매출 통계 처리 완료 - 데이터 크기: {}", result.size());
      return result;

    } catch (Exception e) {
      log.error("매출 통계 처리 중 오류 발생", e);
      throw new RuntimeException("매출 통계 처리 실패", e);
    }
  }

  public Map<String, Object> getParticipantStatistics(LocalDateTime startDate, LocalDateTime endDate) {
    Map<String, Object> result = new HashMap<>();

    // 연령대별 통계
    List<Object[]> ageStats = participantRepository.getAgeDistributionStatistics(startDate, endDate);
    Map<String, Long> ageDistribution = new LinkedHashMap<>();
    for (Object[] stat : ageStats) {
      ageDistribution.put(((Age) stat[0]).toString(), (Long) stat[1]);
    }
    result.put("ageDistribution", ageDistribution);

    // 성별 통계
    List<Object[]> genderStats = participantRepository.getGenderDistributionStatistics(startDate, endDate);
    Map<String, Long> genderDistribution = new LinkedHashMap<>();
    for (Object[] stat : genderStats) {
      genderDistribution.put(stat[0].toString(), (Long) stat[1]);
    }
    result.put("genderDistribution", genderDistribution);

    return result;
  }
}