package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ScheduleDto;
import com.soloProject.myTrip.entity.Item;
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
    public String itemNew(@Valid ItemFormDto itemFormDto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            return "item/itemForm";
        }
        try {
            itemService.saveItem(itemFormDto);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "상품 등록 중 에러가 발생하였습니다.");
            return "item/itemForm";
        }

        return "redirect:/";
    }

    //상품 일정 등록(GET)
    @GetMapping("/admin/{itemId}/schedule/new")
    public String scheduleNew(Model model, @PathVariable("itemId") Long itemId){
        model.addAttribute("scheduleDto", new ScheduleDto(itemId));
        return "item/itemScheduleForm";
    }

    //상품 일정 등록(POST)
    @PostMapping("/admin/item/schedule/new")
    public String scheduleNew(@Valid ScheduleDto scheduleDto, BindingResult bindingResult,
                              @RequestParam List<MultipartFile> imageFile,
                              @RequestParam List<String> description,
                              Model model){
        if(bindingResult.hasErrors()){
            return "item/itemScheduleForm"
        }
    }

    //상품 상세(GET)
    @GetMapping("/item/{itemId}")
    public String itemDtl(@PathVariable("itemId") Long itemId, Model model){
        try{
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId);
            model.addAttribute("itemFormDto", itemFormDto);
            return "item/itemDtl";
        }
        catch (Exception e){
            e.printStackTrace();
            return "redirect:/";
        }
    }

}