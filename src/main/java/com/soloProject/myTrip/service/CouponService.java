package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.CouponStatus;
import com.soloProject.myTrip.dto.CouponDto;
import com.soloProject.myTrip.dto.CouponSearchDto;
import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.entity.CouponWallet;
import com.soloProject.myTrip.entity.Member;
import com.soloProject.myTrip.repository.CouponRepository;
import com.soloProject.myTrip.repository.CouponWalletRepository;
import com.soloProject.myTrip.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponWalletRepository couponWalletRepository;
    private final MemberRepository memberRepository;

    public CouponWallet createCouponWaller(Member member) {
        List<Coupon> coupons = new ArrayList<>();

        Coupon coupon = couponRepository.findByDescriptionAndIsAlphaCouponTrue("가입 축하 쿠폰")
                .orElseThrow(EntityNotFoundException::new);

        Coupon userCoupon = Coupon.giveCoupon(coupon);

        couponRepository.save(userCoupon);
        coupons.add(userCoupon);

        CouponWallet couponWallet = CouponWallet.builder()
                .member(member)
                .coupons(coupons)
                .build();

        userCoupon.setCouponWallet(couponWallet);
        couponWalletRepository.save(couponWallet);
        return couponWallet;
    }

    public void saveCoupon(CouponDto couponDto) {
        Coupon coupon = couponDto.createCoupon();
        couponRepository.save(coupon);
    }

    public void deleteCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(EntityNotFoundException::new);
        couponRepository.delete(coupon);
    }

    @Transactional(readOnly = true)
    public Page<Coupon> getAdminCouponPage(CouponSearchDto searchDto, Pageable pageable) {
        return couponRepository.getAdminCouponPage(searchDto, pageable);
    }

    public void giveCouponForAllMembers(Long couponId) {
        Coupon alphaCoupon = couponRepository.findById(couponId).orElseThrow(EntityNotFoundException::new);
        List<CouponWallet> allCouponWallets = couponWalletRepository.findAll();
        if (!allCouponWallets.isEmpty()) {
            for (CouponWallet couponWallet : allCouponWallets) {
                // 모든 쿠폰지갑에 해당 쿠폰 추가
                Coupon userCoupon = Coupon.giveCoupon(alphaCoupon);
                couponWallet.getCoupons().add(userCoupon);
                userCoupon.setCouponWallet(couponWallet);
            }
        }
    }

    public List<Coupon> getCouponsByMember(String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
        return couponRepository.findByCouponWalletIdAndCouponStatus(member.getCouponWallet().getId(), CouponStatus.USABLE);
    }
}
