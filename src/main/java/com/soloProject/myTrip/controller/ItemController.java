package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ScheduleDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Schedule;
import com.soloProject.myTrip.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 상품 등록(GET)
    @GetMapping("/admin/item/new")
    public String itemNew(Model model) {
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "item/itemForm";
    }

    // 상품 등록(POST)
    @PostMapping("/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
            @RequestParam List<MultipartFile> itemImageFile,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }
        if (!itemFormDto.isValidCategory()){
            model.addAttribute("errorMessage", "선택한 여행 타입에 맞는 카테고리를 선택해주세요.");
        }
        if (itemImageFile.getFirst().isEmpty()) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수입니다.");
            return "item/itemForm";
        }
        if (itemFormDto.getMinParticipants() > itemFormDto.getRemainingSeats()){
            model.addAttribute("errorMessage", "최소 출발 인원이 최대 인원보다 많습니다.");
        }
        try {
            itemService.saveItem(itemFormDto, itemImageFile);
            return "redirect:/admin/items";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }
    }

    // 상품 수정(GET)
    @GetMapping("/admin/item/{itemId}")
    public String itemEdit(Model model, @PathVariable("itemId") Long itemId) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
            return "item/itemForm";
        } catch (Exception e) {
            e.printStackTrace();
            return "item/itemMng";
        }
    }

    // 상품 수정(POST)
    @PostMapping("/admin/item/{itemId}")
    public String itemUpdate(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
            @RequestParam("itemImageFile") List<MultipartFile> itemImageFile,
            Model model) {
        if (bindingResult.hasErrors()) { // 유효성 체크
            return "item/itemForm";
        }
        if (itemImageFile.isEmpty() && itemFormDto.getId() == null) {
            model.addAttribute("errorMessage", "첫 번째 상품 이미지는 필수입니다.");
            return "item/itemForm";
        }
        try {
            itemService.updateItem(itemFormDto, itemImageFile);
            return "redirect:/admin/items";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "상품 업로드 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }
    }

    // 상품 상세(GET)
    @GetMapping("/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model) {
        try {
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
            return "item/itemDtl";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/";
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
            itemService.deleteItemImage(itemId, index);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}