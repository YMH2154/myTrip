package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.constant.Age;
import com.soloProject.myTrip.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByMemberReservationIdAndAge(Long reservationId, Age age);

    // 연령대별 통계
    @Query("SELECT p.age, COUNT(p) " +
           "FROM Participant p " +
           "JOIN p.memberReservation mr " +  // MemberReservation과 조인
           "WHERE p.regTime BETWEEN :startDate AND :endDate " +
           "AND mr.reservationStatus != 'REFUNDED' " +  // 취소된 예약 제외
           "GROUP BY p.age")
    List<Object[]> getAgeDistributionStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 성별 통계
    @Query("SELECT p.sex, COUNT(p) " +
           "FROM Participant p " +
           "JOIN p.memberReservation mr " +  // MemberReservation과 조인
           "WHERE p.regTime BETWEEN :startDate AND :endDate " +
           "AND mr.reservationStatus != 'REFUNDED' " +  // 취소된 예약 제외
           "GROUP BY p.sex")
    List<Object[]> getGenderDistributionStatistics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
