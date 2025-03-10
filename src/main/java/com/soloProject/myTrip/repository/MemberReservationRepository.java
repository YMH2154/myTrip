package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.constant.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {
    Optional<MemberReservation> findByReservationNumber(String reservationNumber);

    List<MemberReservation> findByMemberOrderByRegTimeDesc(Member member);

    List<MemberReservation> findByReservationStatusAndReservedAtBefore(
            ReservationStatus status,
            LocalDateTime dateTime);

    List<MemberReservation> findByItemReservationId(Long itemReservationId);
}