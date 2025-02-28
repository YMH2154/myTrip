package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByAmountAndMemberReservationId(BigDecimal amount, Long memberReservationId);
    Optional<Payment> findByMerchantUid(String merchantUid);
}