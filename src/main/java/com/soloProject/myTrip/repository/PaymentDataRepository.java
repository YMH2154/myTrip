package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.PaymentData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDataRepository extends JpaRepository<PaymentData, Long> {
}
