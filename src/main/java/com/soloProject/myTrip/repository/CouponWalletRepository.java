package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.entity.CouponWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponWalletRepository extends JpaRepository<CouponWallet, Long> {
    CouponWallet findByMemberId(Long memberId);
}
