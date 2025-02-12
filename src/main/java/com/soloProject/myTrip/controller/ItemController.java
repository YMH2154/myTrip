package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    //상품 등록(GET)
    @GetMapping("/admin/item/new")
    public String itemNew(Model model){
        model.addAttribute("itemFormDto", new ItemFormDto());
        return "item/itemForm";
    }
    //상품 등록(POST)
    @PostMapping("/admin/item/new")
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult,
                          @RequestParam("itemImageFile") List<MultipartFile> itemImageFiles,
                          @RequestParam("imageDescriptions") List<String> imageDescriptions,
                          Model model) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }
        if (itemImageFiles.get(0).isEmpty()) {
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값입니다.");
            return "item/itemForm";
        }
        try {
            itemService.saveItem(itemFormDto, itemImageFiles, imageDescriptions);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";
    }
    //상품 상세(GET)
    @GetMapping("/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId){
        try{
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            return "item/itemDtl";
        }
        catch (Exception e){
            e.printStackTrace();
            return "redirect:/";
        }
    }

}