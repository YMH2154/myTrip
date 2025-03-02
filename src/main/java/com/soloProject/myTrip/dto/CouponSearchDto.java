package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.CouponType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponSearchDto {
    private String searchDateType; // 검색 기간 타입
    private String couponType; // 쿠폰 타입 (AMOUNT/PERCENTAGE)
    private int minAmount; // 최소 구매 금액
    private String searchQuery; // 검색어 (쿠폰 설명)
}
