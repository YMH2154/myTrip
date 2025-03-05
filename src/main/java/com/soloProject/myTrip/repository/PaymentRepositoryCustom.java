package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.dto.PaymentSearchDto;
import com.soloProject.myTrip.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepositoryCustom {
    Page<Payment> getAdminPaymentPage(PaymentSearchDto paymentSearchDto, Pageable pageable);
} 