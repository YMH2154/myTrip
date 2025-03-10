package com.soloProject.myTrip.scheduler;

import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponValidCheckScheduler {

    private final CouponRepository couponRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void deleteExpiredCoupons() {
        try {
            LocalDate today = LocalDate.now();
            List<Coupon> expiredCoupons = couponRepository.findExpiredCoupons(today);

            if (!expiredCoupons.isEmpty()) {
                log.info("만료된 쿠폰 {}개 삭제 시작", expiredCoupons.size());
                for (Coupon coupon : expiredCoupons) {
                    couponRepository.delete(coupon);
                    log.info("쿠폰 ID: {} 삭제 완료", coupon.getId());
                }
                log.info("만료된 쿠폰 삭제 완료");
            } else {
                log.info("만료된 쿠폰이 없습니다.");
            }
        } catch (Exception e) {
            log.error("만료된 쿠폰 삭제 중 오류 발생", e);
        }
    }
}
