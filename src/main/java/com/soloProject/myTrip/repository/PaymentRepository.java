package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByPriceAndMemberReservationId(String price, Long memberReservationId);
}