package com.soloProject.myTrip.initializer;

import com.soloProject.myTrip.constant.CouponDuration;
import com.soloProject.myTrip.constant.CouponType;
import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.repository.CouponRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class InitCoupon implements CommandLineRunner {

  private final CouponRepository couponRepository;

  public InitCoupon(CouponRepository couponRepository) {
    this.couponRepository = couponRepository;
  }

  @Override
  public void run(String... args) {
    // 기존 쿠폰이 없을 경우에만 초기 쿠폰 생성
    if (couponRepository.count() == 0) {
      Coupon welcomeCoupon = new Coupon();
      welcomeCoupon.setCouponType(CouponType.AMOUNT);
      welcomeCoupon.setDescription("가입 축하 쿠폰");
      welcomeCoupon.setDiscountAmount(new BigDecimal("2000"));
      welcomeCoupon.setCouponDuration(CouponDuration.MONTH);
      welcomeCoupon.setMinPurchaseAmount(new BigDecimal("100000"));

      couponRepository.save(welcomeCoupon);
      System.out.println("가입 축하 쿠폰 생성 완료");
    }
  }
}
