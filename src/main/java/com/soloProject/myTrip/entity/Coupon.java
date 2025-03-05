package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.constant.CouponDuration;
import com.soloProject.myTrip.constant.CouponStatus;
import com.soloProject.myTrip.constant.CouponType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    private int discountAmount; // 할인 금액(type amount)
    private int discountPercentage; // 할인 퍼센트(type percentage)

    private LocalDate startDate;
    private LocalDate endDate;

    private int minPurchaseAmount; // 최소 구매 금액

    private String description;

    @Enumerated(EnumType.STRING)
    private CouponDuration couponDuration;

    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_wallet_id")
    private CouponWallet couponWallet;

    @Column(name = "is_alpha_coupon")
    private boolean isAlphaCoupon;

    public static Coupon giveCoupon(Coupon coupon) {
        Coupon userCoupon = new Coupon();
        userCoupon.setDescription(coupon.description);
        userCoupon.setDiscountAmount(coupon.discountAmount);
        userCoupon.setDiscountPercentage(coupon.discountPercentage);
        userCoupon.setMinPurchaseAmount(coupon.minPurchaseAmount);
        userCoupon.setCouponType(coupon.getCouponType());
        userCoupon.setCouponStatus(CouponStatus.USABLE);
        userCoupon.setAlphaCoupon(false);

        // 유효 기간 설정을 간단하게 리팩토링
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = getCouponEndDate(startDate, coupon.couponDuration);

        userCoupon.setStartDate(startDate);
        userCoupon.setEndDate(endDate);

        return userCoupon;
    }

    private static LocalDate getCouponEndDate(LocalDate startDate, CouponDuration duration) {
        return switch (duration) {
            case WEEK -> startDate.plusWeeks(1);
            case MONTH -> startDate.plusMonths(1);
            case THREE -> startDate.plusMonths(3);
            case SIX -> startDate.plusMonths(6);
            default -> startDate.plusYears(1); // 기본 1년
        };
    }
}
