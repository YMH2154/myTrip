package com.soloProject.myTrip.repository;

import com.soloProject.myTrip.dto.CouponSearchDto;
import com.soloProject.myTrip.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponRepositoryCustom {
  Page<Coupon> getAdminCouponPage(CouponSearchDto searchDto, Pageable pageable);
}