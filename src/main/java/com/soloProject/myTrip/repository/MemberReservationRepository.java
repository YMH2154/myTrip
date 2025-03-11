package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.entity.MemberReservation;
import com.soloProject.myTrip.constant.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 기간별 예약 통계를 위한 JPQL 쿼리
    @Query("SELECT DATE_FORMAT(mr.reservedAt, :pattern) as date, COUNT(mr) " +
           "FROM MemberReservation mr " +
           "WHERE mr.reservedAt BETWEEN :startDate AND :endDate " +
           "AND mr.reservationStatus != 'REFUNDED' " +
           "GROUP BY DATE_FORMAT(mr.reservedAt, :pattern)")
    List<Object[]> getReservationStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("pattern") String pattern
    );
}