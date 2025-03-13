package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ErrorResponse;
import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final MainService mainService;
    private final int PAGE_SIZE = 5;

    @GetMapping(value = "/")
    public String mainPage(Model model) {
        List<Item> recommendedItems = mainService.getRecommendedItems();
        List<Item> cheapItems = mainService.getMostCheapItems();
        List<Item> reservationCountItems = mainService.getMostReservationCountItems();

        model.addAttribute("recommendedItems", recommendedItems);
        model.addAttribute("cheapItems", cheapItems);
        model.addAttribute("reservationCountItems", reservationCountItems);

        return "main";
    }

    // 통합된 검색/카테고리 페이지
    @GetMapping("/search")
    public String itemsPage(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "searchQuery", required = false) String searchQuery,
            @RequestParam(value = "category", required = false) String category,
            Model model) {
        try {
            int pageSize = PAGE_SIZE;
            List<Item> items;
            boolean isSearch = searchQuery != null && !searchQuery.isEmpty();

            if (isSearch) {
                items = mainService.getSearchItemPage(searchQuery, page, pageSize);
                model.addAttribute("searchQuery", searchQuery);
            } else {
                items = mainService.getItemByCategory(category, page, pageSize);
                model.addAttribute("categoryLink", category);
                model.addAttribute("categoryName", getCategoryDisplayName(category));
            }

            model.addAttribute("items", items);
            model.addAttribute("currentPage", page);
            model.addAttribute("hasNext", items.size() == pageSize);
            model.addAttribute("isSearch", isSearch);

            return "item/itemSearchPage";
        } catch (Exception e) {
            log.error("아이템 조회 중 에러 발생", e);
            return "redirect:/";
        }
    }

    // 통합된 API 엔드포인트 - 무한 스크롤용
    @GetMapping("/search/api")
    @ResponseBody
    public ResponseEntity<?> getItems(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize,
            @RequestParam(value = "searchQuery", required = false) String searchQuery,
            @RequestParam(value = "category", required = false) String category) {
        try {
            log.info("아이템 요청 - 검색어: {}, 카테고리: {}, 페이지: {}", searchQuery, category, page);

            List<Item> items;
            if (searchQuery != null && !searchQuery.isEmpty()) {
                items = mainService.getSearchItemPage(searchQuery, page, pageSize);
            } else {
                items = mainService.getItemByCategory(category, page, pageSize);
            }

            List<Map<String, Object>> simplifiedItems = items.stream().map(item -> {
                Map<String, Object> simplifiedItem = new HashMap<>();
                simplifiedItem.put("id", item.getId());
                simplifiedItem.put("itemName", item.getItemName());
                simplifiedItem.put("thumbnailImageUrls", item.getThumbnailImageUrls());
                simplifiedItem.put("night", item.getNight());
                simplifiedItem.put("duration", item.getDuration());
                simplifiedItem.put("lowestPrice", item.getLowestPrice());
                simplifiedItem.put("price", item.getPrice());
                simplifiedItem.put("earliestDepartureDate", item.getEarliestDepartureDate());

                // 스케줄 정보 간소화
                if (item.getSchedules() != null) {
                    List<Map<String, String>> schedules = item.getSchedules().stream()
                            .limit(3)
                            .map(schedule -> {
                                Map<String, String> scheduleMap = new HashMap<>();
                                scheduleMap.put("activity", schedule.getActivity());
                                return scheduleMap;
                            })
                            .collect(Collectors.toList());
                    simplifiedItem.put("schedules", schedules);
                }

                return simplifiedItem;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("items", simplifiedItems);
            response.put("hasNext", items.size() == pageSize);
            response.put("currentPage", page);
            response.put("totalItems", items.size());

            log.info("아이템 응답 - 아이템 수: {}, 다음 페이지 존재: {}", items.size(), items.size() == pageSize);

            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            log.error("아이템 로딩 중 에러 발생", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    // 카테고리 표시 이름 변환
    private String getCategoryDisplayName(String category) {
        return switch (category.toLowerCase()) {
            case "domestic" -> "국내여행";
            case "theme" -> "테마여행";
            case "asia" -> "아시아";
            case "europe" -> "유럽";
            case "america" -> "미주";
            default -> category;
        };
    }
}