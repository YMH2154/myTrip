package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.CouponDuration;
import com.soloProject.myTrip.constant.CouponStatus;
import com.soloProject.myTrip.constant.CouponType;
import com.soloProject.myTrip.entity.Coupon;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CouponDto {

    @NotNull(message = "쿠폰 사용 기간을 선택해주세요")
    private CouponDuration couponDuration; //쿠폰 유효 기간
    private CouponType couponType;
    @NotNull(message = "할인 금액을 입력해주세요")
    private int discountAmount;
    @NotNull(message = "할인 비율을 입력해주세요")
    private int discountPercentage;
    private String description;
    @NotNull(message = "사용 가능 금액을 입력해주세요")
    private Integer minPurchaseAmount;

    //사용자용 쿠폰 상태 (미사용, 사용)
    private CouponStatus couponStatus;

    public Coupon createCoupon(){
        Coupon coupon = new Coupon();
        coupon.setCouponType(this.couponType);
        coupon.setDescription(this.description);
        coupon.setCouponDuration(this.couponDuration);
        coupon.setMinPurchaseAmount(this.minPurchaseAmount);
        coupon.setCouponStatus(CouponStatus.USABLE);
        coupon.setIsAlphaCoupon(true);
        if(this.couponType.equals(CouponType.AMOUNT)){
            coupon.setDiscountAmount(this.discountAmount);
        }else{
            coupon.setDiscountPercentage(this.discountPercentage);
        }
        return coupon;
    }
}
