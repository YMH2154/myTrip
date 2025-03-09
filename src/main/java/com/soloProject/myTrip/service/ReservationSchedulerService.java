package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.ReservationStatus;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.repository.MemberReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationSchedulerService {

  private final MemberReservationRepository memberReservationRepository;
  private final ReservationService reservationService;

  @Scheduled(fixedDelay = 60000) // 1분마다 실행
  @Transactional
  public void cancelUnpaidReservations() {
    log.info("미결제 예약 취소 작업 시작");
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

    List<MemberReservation> unpaidReservations = memberReservationRepository
        .findByReservationStatusAndReservedAtBefore(
            ReservationStatus.RESERVED,
            oneHourAgo);

    for (MemberReservation reservation : unpaidReservations) {
      try {
        log.info("미결제 예약 취소 처리 - 예약번호: {}", reservation.getReservationNumber());
        reservationService.cancelReservation(reservation.getReservationNumber(), null);
      } catch (Exception e) {
        log.error("예약 취소 실패 - 예약번호: {}", reservation.getReservationNumber(), e);
      }
    }
    log.info("미결제 예약 취소 작업 완료");
  }
}