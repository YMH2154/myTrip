package com.soloProject.myTrip.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController
{
    @GetMapping("/admin")
    public String adminPage(){
        return "admin/admin";
    }

    //상품 등록

    //배너 등록

    //상품 조회(예약자 정보, 상품 정보)

    //결제 기록 조회(아이디, 날짜, 결제수단?, 환불여부, 항공권수수료/여행상품)
    //예약(3시간 후 자동 취소)
    //이용자의 환불 요청 => 관리자 환불 승인

    //통계 조회
}
