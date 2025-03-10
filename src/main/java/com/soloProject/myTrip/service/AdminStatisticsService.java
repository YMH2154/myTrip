package com.soloProject.myTrip.service;

import com.soloProject.myTrip.entity.*;
import com.soloProject.myTrip.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminStatisticsService {

  private final MemberReservationRepository memberReservationRepository;
  private final PaymentRepository paymentRepository;
  private final ParticipantRepository participantRepository;
  private final ItemRepository itemRepository;

  public Map<String, Object> getReservationStatistics() {
    Map<String, Object> result = new HashMap<>();

    // 최근 6개월 예약 현황
    List<MemberReservation> recentReservations = memberReservationRepository
        .findByReservedAtAfter(LocalDateTime.now().minusMonths(6));

    Map<String, Long> monthlyReservations = recentReservations.stream()
        .collect(Collectors.groupingBy(
            reservation -> reservation.getReservedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
            Collectors.counting()));

    result.put("monthlyReservations", monthlyReservations);

    // 예약 상태별 통계
    Map<String, Long> statusCount = recentReservations.stream()
        .collect(Collectors.groupingBy(
            reservation -> reservation.getReservationStatus().toString(),
            Collectors.counting()));

    result.put("statusCount", statusCount);

    return result;
  }

  public Map<String, Object> getSalesStatistics() {
    Map<String, Object> result = new HashMap<>();

    // 최근 6개월 매출 현황
    List<Payment> recentPayments = paymentRepository
        .findByRegTimeAfter(LocalDateTime.now().minusMonths(6));

    Map<String, Integer> monthlySales = recentPayments.stream()
        .collect(Collectors.groupingBy(
            payment -> payment.getRegTime().format(DateTimeFormatter.ofPattern("yyyy-MM")),
            Collectors.summingInt(Payment::getAmount)));

    result.put("monthlySales", monthlySales);

    // 결제 수단별 통계
    Map<String, Long> paymentMethodCount = recentPayments.stream()
        .collect(Collectors.groupingBy(
            payment -> payment.getPaymentMethod().toString(),
            Collectors.counting()));

    result.put("paymentMethodCount", paymentMethodCount);

    return result;
  }

  public Map<String, Object> getParticipantStatistics() {
    Map<String, Object> result = new HashMap<>();

    // 연령대별 통계
    List<Participant> allParticipants = participantRepository.findAll();
    Map<String, Long> ageDistribution = allParticipants.stream()
        .collect(Collectors.groupingBy(
            participant -> participant.getAge().toString(),
            Collectors.counting()));

    result.put("ageDistribution", ageDistribution);

    // 성별 통계
    Map<String, Long> genderDistribution = allParticipants.stream()
        .collect(Collectors.groupingBy(
            participant -> participant.getSex().toString(),
            Collectors.counting()));

    result.put("genderDistribution", genderDistribution);

    return result;
  }
}