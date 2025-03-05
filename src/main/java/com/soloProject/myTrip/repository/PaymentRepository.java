package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.constant.PaymentType;
import com.soloProject.myTrip.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository
                extends JpaRepository<Payment, Long>, QuerydslPredicateExecutor<Payment>, PaymentRepositoryCustom {
        Optional<Payment> findByMerchantUid(String merchantUid);

        Payment findByMemberReservationIdAndPaymentType(Long memberReservationId, PaymentType paymentType);

        boolean existsByMemberReservationIdAndPaymentType(Long memberReservationId, PaymentType paymentType);
}