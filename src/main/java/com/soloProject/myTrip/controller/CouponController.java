package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.CouponDto;
import com.soloProject.myTrip.dto.CouponSearchDto;
import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CouponController {
    private final CouponService couponService;

    @GetMapping("/admin/coupon/new")
    public String newCoupon(Model model) {
        model.addAttribute("couponForm", new CouponDto());
        return "coupon/couponForm";
    }

    @PostMapping("/admin/coupon/new")
    public String newCoupon(@Valid CouponDto couponDto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "coupon/new";
        }
        try {
            couponService.saveCoupon(couponDto);
            return "redirect:/admin/coupons";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "쿠폰 생성 중 에러 발생");
            return "coupon/couponForm";
        }
    }

    @DeleteMapping("/admin/coupon/{couponId}")
    @ResponseBody
    public ResponseEntity<String> deleteCoupon(@PathVariable("couponId") Long couponId) {
        try {
            couponService.deleteCoupon(couponId);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 쿠폰 검색 API
    @GetMapping("/admin/coupons/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchCoupons(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "searchDateType", defaultValue = "all") String searchDateType,
            @RequestParam(value = "couponType", required = false) String couponType,
            @RequestParam(value = "minAmount", defaultValue = "0") int minAmount,
            @RequestParam(value = "searchQuery", required = false) String searchQuery) {

        try {
            CouponSearchDto searchDto = new CouponSearchDto();
            searchDto.setSearchDateType(searchDateType);
            searchDto.setCouponType(couponType);
            searchDto.setMinAmount(minAmount);
            searchDto.setSearchQuery(searchQuery);

            Pageable pageable = PageRequest.of(page, 10);
            Page<Coupon> coupons = couponService.getAdminCouponPage(searchDto, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("coupons", coupons.getContent());
            response.put("currentPage", coupons.getNumber());
            response.put("totalPages", coupons.getTotalPages());
            response.put("totalElements", coupons.getTotalElements());
            response.put("size", coupons.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
