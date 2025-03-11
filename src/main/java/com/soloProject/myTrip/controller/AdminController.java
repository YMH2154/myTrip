package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.*;
import com.soloProject.myTrip.entity.*;
import com.soloProject.myTrip.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ItemService itemService;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final QnAService qnAService;

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
            return "error/error";
        }
    }

    // 쿠폰 관리 페이지
    @GetMapping({ "/admin/coupons", "/admin/coupons/{page}" })
    public String couponMngPage(@PathVariable("page") Optional<Integer> page,
            CouponSearchDto couponSearchDto,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page.orElse(0), 10);
            Page<Coupon> coupons = couponService.getAdminCouponPage(couponSearchDto, pageable);

            model.addAttribute("coupons", coupons);
            model.addAttribute("couponSearchDto", couponSearchDto);
            model.addAttribute("maxPage", 5);

            return "coupon/couponMng";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }

    // 결제 관리 페이지
    @GetMapping({ "/admin/payments", "/admin/payments/{page}" })
    public String paymentMngPage(@PathVariable("page") Optional<Integer> page,
            PaymentSearchDto paymentSearchDto,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page.orElse(0), 10);
            Page<Payment> payments = paymentService.getAdminPaymentPage(paymentSearchDto, pageable);

            model.addAttribute("payments", payments);
            model.addAttribute("paymentSearchDto", paymentSearchDto);
            model.addAttribute("maxPage", 5);

            return "payment/paymentMng";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }

    // Q&A 관리 페이지
    @GetMapping({ "/admin/qnas", "/admin/qnas/{page}" })
    public String qnaMngPage(@PathVariable("page") Optional<Integer> page, Model model) {
        try {
            Pageable pageable = PageRequest.of(page.orElse(0), 10, Sort.by(Sort.Order.desc("regTime")));
            Page<QnA> qnAs = qnAService.getAdminQnAPage(pageable);

            model.addAttribute("qnAs", qnAs);
            model.addAttribute("maxPage", 5);
            return "qna/qnaMng";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/error";
        }
    }

    // 통계 조회 페이지
    @GetMapping("/admin/statistics")
    public String statistics(Model model) {
        log.info("통계 페이지 접근");
        return "admin/statistics";
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

    @ModelAttribute("unansweredCount")
    public Long getUnansweredCount() {
        return qnAService.getUnansweredCount();
    }

    @GetMapping("/items")
    public String itemsPage() {
        return "admin/items";
    }

    @GetMapping("/coupons")
    public String couponsPage() {
        return "admin/coupons";
    }

    @GetMapping("/payments")
    public String paymentsPage() {
        return "admin/payments";
    }
}
