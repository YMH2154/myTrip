package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ItemReservationDto;
import com.soloProject.myTrip.dto.ScheduleDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;
    private final ScheduleService scheduleService;
    private final ItemReservationService itemReservationService;
    private final RecentViewService recentViewService;
    private final QnAService qnAService;

    @ModelAttribute("unansweredCount")
    public Long getUnansweredCount() {
        return qnAService.getUnansweredCount();
    }

    // 상품 등록(GET)
    @GetMapping("/admin/item/new")
    public String itemNew(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "item/itemForm";
    }

    // 상품 등록(POST)
    @PostMapping("/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto,
            BindingResult bindingResult,
            @RequestParam List<MultipartFile> thumbnailImageFile,
            @RequestParam MultipartFile itemDetailImageFile,
            Model model) {

        // 바인딩 에러 처리
        if (bindingResult.hasErrors()) {
            log.error("바인딩 에러 발생: {}", bindingResult.getAllErrors());
            bindingResult.getFieldErrors().forEach(error -> {
                log.error("필드 에러 - 필드명: {}, 거부된 값: {}, 에러 메시지: {}",
                        error.getField(), error.getRejectedValue(), error.getDefaultMessage());
            });
            return "item/itemForm";
        }

        // 유효성 검사
        if (itemDetailImageFile.isEmpty()) {
            model.addAttribute("errorMessage", "상품 상세 설명 이미지는 필수입니다.");
            return "item/itemForm";
        }
        if (thumbnailImageFile.getFirst().isEmpty()) {
            model.addAttribute("errorMessage", "첫 번째 썸네일 이미지는 필수입니다.");
            return "item/itemForm";
        }
        if (itemFormDto.getMinParticipants() > itemFormDto.getMaxParticipants()) {
            model.addAttribute("errorMessage", "최소 출발 인원이 최대 인원보다 많습니다.");
            return "item/itemForm";
        }
        if (itemFormDto.getNight() >= itemFormDto.getDuration()) {
            model.addAttribute("errorMessage", "여행 기간이 올바르지 않습니다.");
            return "item/itemForm";
        }
        if (itemFormDto.isValidCategory()) {
            model.addAttribute("errorMessage", "선택한 여행 타입에 맞는 카테고리를 선택해주세요.");
            return "item/itemForm";
        }

        try {
            itemService.saveItem(itemFormDto, itemDetailImageFile, thumbnailImageFile);
            return "redirect:/admin/items";
        } catch (Exception e) {
            log.error("상품 등록 중 에러 발생", e);
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }
    }

    // 상품 수정(GET)
    @GetMapping("/admin/item/{itemId}")
    public String itemEdit(Model model, @PathVariable("itemId") Long itemId) {
        try {
            ItemFormDto itemFormDto = itemService.getItem(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
            return "item/itemForm";
        } catch (Exception e) {
            e.printStackTrace();
            return "item/itemMng";
        }
    }

    // 상품 수정(POST)
    @PostMapping("/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto,
            BindingResult bindingResult,
            @RequestParam("thumbnailImageFile") List<MultipartFile> thumbnailImages,
            @RequestParam("itemDetailImageFile") MultipartFile itemDetailImageFile,
            Model model) {

        log.info("상품 수정 요청 시작 - 상품 ID: {}", itemFormDto.getId());

        if (bindingResult.hasErrors()) {
            log.error("바인딩 에러 발생: {}", bindingResult.getAllErrors());
            bindingResult.getFieldErrors().forEach(error -> {
                log.error("필드 에러 - 필드명: {}, 거부된 값: {}, 에러 메시지: {}",
                        error.getField(), error.getRejectedValue(), error.getDefaultMessage());
            });
            return "item/itemForm";
        }

        // 기존 상품 정보 조회
        ItemFormDto existingItem = itemService.getItem(itemFormDto.getId());

        // 유효성 검사
        if (itemDetailImageFile.isEmpty() && existingItem.getItemDetailImageUrl() == null) {
            model.addAttribute("errorMessage", "상품 상세 설명 이미지는 필수입니다.");
            return "item/itemForm";
        }

        if (thumbnailImages.stream().allMatch(MultipartFile::isEmpty) &&
                (existingItem.getThumbnailImageUrls() == null || existingItem.getThumbnailImageUrls().isEmpty())) {
            model.addAttribute("errorMessage", "최소 하나의 썸네일 이미지는 필수입니다.");
            return "item/itemForm";
        }

        if (itemFormDto.getMinParticipants() > itemFormDto.getMaxParticipants()) {
            model.addAttribute("errorMessage", "최소 출발 인원이 최대 인원보다 많습니다.");
            return "item/itemForm";
        }

        if (itemFormDto.getNight() >= itemFormDto.getDuration()) {
            model.addAttribute("errorMessage", "여행 기간이 올바르지 않습니다.");
            return "item/itemForm";
        }

        if (itemFormDto.isValidCategory()) {
            model.addAttribute("errorMessage", "선택한 여행 타입에 맞는 카테고리를 선택해주세요.");
            return "item/itemForm";
        }

        try {
            // 기존 이미지 URL 전달
            itemFormDto.setItemDetailImageUrl(existingItem.getItemDetailImageUrl());
            itemFormDto.setThumbnailImageUrls(existingItem.getThumbnailImageUrls());

            itemService.updateItem(itemFormDto, thumbnailImages, itemDetailImageFile);
            return "redirect:/admin/items";
        } catch (Exception e) {
            log.error("상품 수정 중 에러 발생", e);
            model.addAttribute("errorMessage", "상품 수정 중 오류가 발생했습니다.");
            return "item/itemForm";
        }
    }

    // 여행 상품 삭제(ajax)
    @DeleteMapping("/admin/item/{itemId}")
    @ResponseBody
    public ResponseEntity<String> deleteItem(@PathVariable("itemId") Long itemId) {
        try {
            itemService.deleteItem(itemId);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 여행 상품 이미지 삭제(ajax)
    @DeleteMapping("/admin/item/{itemId}/image/{index}")
    @ResponseBody
    public ResponseEntity<String> deleteItemImage(@PathVariable("itemId") Long itemId,
            @PathVariable("index") int index) {
        try {
            itemService.deleteThumbnailImage(itemId, index);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // 여행 상품 날짜 선택 화면(달력)
    @GetMapping("/item/{itemId}/calendar")
    public String itemCalendar(@PathVariable("itemId") Long itemId, Model model) {
        try {
            ItemFormDto item = itemService.getItem(itemId);
            ItemReservationDto reservationInfo = itemReservationService.getItemReservationInfo(itemId);

            model.addAttribute("item", item);
            model.addAttribute("reservationInfo", reservationInfo);

            return "item/itemCalendar";
        } catch (Exception e) {
            log.error("Failed to load calendar view: {}", e.getMessage());
            return "main";
        }
    }

    // 상세 페이지
    @GetMapping("/item/{itemId}/detail")
    public String itemDetail(@PathVariable("itemId") Long itemId,
            @RequestParam("departureDateTime") String departureDateTime,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {
        try {
            ItemFormDto item = itemService.getItem(itemId);
            ItemReservation reservation = itemReservationService.getItemReservation(
                    itemId, departureDateTime);
            List<ScheduleDto> schedules = scheduleService.getScheduleDtl(itemId);

            // 최근 본 상품 저장 (쿠키 사용)
            recentViewService.addRecentView(request, response, itemId);

            // 최근 본 다른 상품들 조회 (쿠키 사용)
            List<Item> recentItems = recentViewService.getRecentItems(request, itemId);

            model.addAttribute("item", item);
            model.addAttribute("reservation", reservation);
            model.addAttribute("schedules", schedules);
            model.addAttribute("recentItems", recentItems);

            return "item/itemDtl";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
        }
    }
}