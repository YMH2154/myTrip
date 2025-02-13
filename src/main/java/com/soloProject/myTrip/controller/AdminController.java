package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ItemSearchDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AdminController{

    private final ItemService itemService;

    @GetMapping("/admin")
    public String adminPage(){
        return "admin/admin";
    }

    //상품 관리 페이지
    @GetMapping({"/admin/items", "/admin/items/{page}"})
    public String itemMngPage(@PathVariable("page")Optional<Integer> page, ItemSearchDto itemSearchDto, Model model){
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 5);
        Page<Item> items = itemService.getAdminItemPage(itemSearchDto, pageable);
        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);
        return "item/itemMng";
    }

    //배너 등록

    //상품 조회(예약자 정보, 상품 정보)

    //결제 기록 조회(아이디, 날짜, 결제수단?, 환불여부, 항공권수수료/여행상품)
    //예약(3시간 후 자동 취소)
    //이용자의 환불 요청 => 관리자 환불 승인

    //통계 조회
}
