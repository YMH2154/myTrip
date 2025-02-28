package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.BannnerFormDto;
import com.soloProject.myTrip.dto.ItemSearchDto;
import com.soloProject.myTrip.dto.CouponSearchDto;
import com.soloProject.myTrip.entity.Banners;
import com.soloProject.myTrip.entity.Coupon;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.service.BannersService;
import com.soloProject.myTrip.service.CouponService;
import com.soloProject.myTrip.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final ItemService itemService;
    private final BannersService bannersService;
    private final CouponService couponService;

    // 상품 관리 페이지
    @GetMapping({ "/admin", "/admin/items", "/admin/items/{page}" })
    public String itemMngPage(@PathVariable("page") Optional<Integer> page, ItemSearchDto itemSearchDto, Model model) {
        try {
            Pageable pageable = PageRequest.of(page.orElse(0), 5);
            Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);
            model.addAttribute("items", items);
            model.addAttribute("itemSearchDto", itemSearchDto);
            model.addAttribute("maxPage", 5);
            return "item/itemMng";
        } catch (Exception e) {
            e.printStackTrace();
            return "item/itemMng";
        }
    }

    // 배너 관리 페이지
    @GetMapping("/admin/banners")
    public String bannerMngPage(Model model) {
        List<Banners> banners = bannersService.getBannerList();
        List<BannnerFormDto> bannerDtos = banners.stream()
                .map(BannnerFormDto::of)
                .collect(Collectors.toList());

        model.addAttribute("banners", bannerDtos);
        return "banner/bannerMng";
    }

    // 쿠폰 관리 페이지
    @GetMapping({ "/admin/coupons", "/admin/coupons/{page}" })
    public String couponMngPage(@PathVariable("page") Optional<Integer> page,
            CouponSearchDto couponSearchDto,
            Model model) {
        Pageable pageable = PageRequest.of(page.orElse(0), 10);
        Page<Coupon> coupons = couponService.getAdminCouponPage(couponSearchDto, pageable);

        model.addAttribute("coupons", coupons);
        model.addAttribute("couponSearchDto", couponSearchDto);
        model.addAttribute("maxPage", 5);

        return "coupon/couponMng";
    }


    // 액셀 다운로드
    // @GetMapping("/admin/analytics/download")
    // public ResponseEntity<InputStreamResource> downloadExcel() {
    // try {
    // ByteArrayInputStream in = excelService.generateExcelReport();
    //
    // HttpHeaders headers = new HttpHeaders();
    // headers.add("Content-Disposition", "attachment;
    // filename=content-analysis.xlsx");
    //
    // return ResponseEntity
    // .ok()
    // .headers(headers)
    // .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
    // .body(new InputStreamResource(in));
    // } catch (Exception e) {
    // return ResponseEntity.badRequest().build();
    // }
    // }

    // 대시보드
    // @GetMapping("/admin/dashboard")
    // public String dashboard(Model model) {
    // model.addAttribute("totalVisitors", visitorService.getTotalVisitorCount());
    // model.addAttribute("monthlyVisitors", visitorService.getMonthlyStats());
    // model.addAttribute("dailyVisitors", visitorService.getDailyStats());
    // model.addAttribute("todayVisitors", visitorService.getTodayVisitorCount());
    // model.addAttribute("recentDailyStats", visitorService.getRecentDailyStats());
    // model.addAttribute("monthlyTrending", tmdbService.getMonthlyTrending());
    // model.addAttribute("weeklyTrending", tmdbService.getWeeklyTrending());
    // // Payment 데이터를 이용한 주간 매출 데이터
    // Map<String, Long> weeklyRevenue = subscribeService.calculateWeeklyRevenue();
    // model.addAttribute("weeklyRevenue", weeklyRevenue);
    // // 총 매출 데이터 추가
    // Long totalRevenue = subscribeService.calculateTotalRevenue();
    // model.addAttribute("totalRevenue", totalRevenue);
    // // 주간 총 매출 계산
    // Long weeklyTotalRevenue =
    // weeklyRevenue.values().stream().mapToLong(Long::longValue).sum();
    // model.addAttribute("weeklyTotalRevenue", weeklyTotalRevenue);
    //
    // // 일일 매출 데이터
    // Map<String, Long> dailyRevenue = subscribeService.calculateDailyRevenue();
    // model.addAttribute("dailyRevenue", dailyRevenue);
    //
    // return "admin/dashboard";
    // }

    // 배너 등록

    // 상품 조회(예약자 정보, 상품 정보)

    // 결제 기록 조회(아이디, 날짜, 결제수단?, 환불여부, 항공권수수료/여행상품)
    // 예약(3시간 후 자동 취소)
    // 이용자의 환불 요청 => 관리자 환불 승인

    // 통계 조회
}
