package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.BannnerFormDto;
import com.soloProject.myTrip.dto.ErrorResponse;
import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.service.BannersService;
import com.soloProject.myTrip.service.FlightSearchService;
import com.soloProject.myTrip.service.ItemService;
import com.soloProject.myTrip.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final MainService mainService;
    private final ItemService itemService;
    private final BannersService bannersService;

    @GetMapping(value = "/")
    public String main(Model model) {
        List<Item> recommendedItems = mainService.getRecommendedItems();
        List<Item> confirmedItems = mainService.getConfirmedItems();
        List<BannnerFormDto> banners = bannersService.getLatestContents().stream().limit(5).toList();

        model.addAttribute("recommendedItems", recommendedItems);
        model.addAttribute("confirmedItems", confirmedItems);
        model.addAttribute("banners", banners);

        return "main";
    }

    // 검색 결과 페이지
    @GetMapping("/search")
    public String searchPage(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam("searchQuery") String searchQuery,
            Model model) {
        try {
            int pageSize = 12;
            List<ItemFormDto> items = itemService.getSearchItemPage(searchQuery, page, pageSize);

            model.addAttribute("items", items);
            model.addAttribute("searchQuery", searchQuery);
            model.addAttribute("currentPage", page);
            model.addAttribute("hasNext", items.size() == pageSize);

            return "item/itemSearchPage";
        } catch (Exception e) {
            log.error("검색 중 에러 발생", e);
            return "redirect:/";
        }
    }

    // 카테고리 페이지
    @GetMapping("/category/{link}")
    public String categoryPage(@PathVariable("link") String link,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        try {
            List<ItemFormDto> items = itemService.getItemByCategory(link, page, 12);
            model.addAttribute("items", items);
            model.addAttribute("categoryLink", link);
            return "item/itemSearchPage";
        } catch (Exception e) {
            log.error("카테고리 결과 호출 중 에러 발생", e);
            return "redirect:/";
        }
    }

    // API 엔드포인트 - 무한 스크롤용
    @GetMapping("/api/category/{link}")
    @ResponseBody
    public ResponseEntity<?> getCategoryItems(@PathVariable("link") String link,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        try {
            List<ItemFormDto> items = itemService.getItemByCategory(link, page, 12);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            log.error("카테고리 아이템 로딩 중 에러 발생", e);
            return ResponseEntity.badRequest().body(new ErrorResponse("요청 실패: " + e.getMessage()));
        }
    }
}