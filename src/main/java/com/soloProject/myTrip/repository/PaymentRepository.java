package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.constant.PaymentType;
import com.soloProject.myTrip.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long>, QuerydslPredicateExecutor<Payment>, PaymentRepositoryCustom {
    Optional<Payment> findByMerchantUid(String merchantUid);

    Payment findByMemberReservationIdAndPaymentType(Long memberReservationId, PaymentType paymentType);

    boolean existsByMemberReservationIdAndPaymentType(Long memberReservationId, PaymentType paymentType);

    // 기간별 매출 통계를 위한 JPQL 쿼리
    @Query("SELECT DATE_FORMAT(p.regTime, :pattern) as date, SUM(p.amount) " +
            "FROM Payment p " +
            "JOIN p.memberReservation mr " +
            "WHERE p.regTime BETWEEN :startDate AND :endDate " +
            "AND mr.reservationStatus != 'REFUNDED' " +
            "GROUP BY DATE_FORMAT(p.regTime, :pattern)")
    List<Object[]> getSalesStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pattern") String pattern);

}