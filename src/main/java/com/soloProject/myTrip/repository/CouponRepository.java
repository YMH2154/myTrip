package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.constant.CouponStatus;
import com.soloProject.myTrip.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {

    Optional<Coupon> findByDescription(String description);

    List<Coupon> findByEndDateBeforeAndCouponStatus(LocalDate endDate, CouponStatus status);

    @Query("SELECT c FROM Coupon c " +
            "WHERE c.couponStatus = 'ACTIVE' " +
            "AND c.startDate <= CURRENT_DATE " +
            "AND c.endDate >= CURRENT_DATE " +
            "AND c.minPurchaseAmount <= :minAmount")
    List<Coupon> findAvailableCoupons(@Param("minAmount") Long minAmount, @Param("now") LocalDateTime now);
}
