package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.BannnerFormDto;
import com.soloProject.myTrip.service.BannersService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class BannersController {

    private final BannersService bannerService;

    // 배너 등록 페이지
    @GetMapping("/admin/banners/new")
    public String bannerForm(Model model) {
        model.addAttribute("bannnerFormDto", new BannnerFormDto());
        return "banner/bannerForm";
    }

    // 배너등록
    @PostMapping("/admin/banners/new")
    public String bannerNew(@Valid BannnerFormDto bannnerFormDto, BindingResult bindingResult, @RequestParam("bannerImgFile") MultipartFile bannerImgFile, Model model) {
        if (bindingResult.hasErrors()) {  // 유효성 체크
            return "banner/bannerForm";
        }
        if(bannerImgFile.isEmpty()&&bannnerFormDto.getId() == null) {
            model.addAttribute("errorMessage", "배너 이미지는 필수 입력 값입니다.");
            return "banner/bannerForm";
        }
        try {
            bannerService.saveBanner(bannnerFormDto, bannerImgFile);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "배너 등록 중 오류가 발생했습니다.");
            return "banner/bannerForm";
        }
        return "redirect:/admin/banners";
    }

    // 배너 삭제
    @DeleteMapping("/admin/banners/{bannerId}")
    @ResponseBody
    public ResponseEntity deleteBanner(@PathVariable("bannerId") Long bannerId){
        try{
            bannerService.deleteBanner(bannerId);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>("삭제 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("삭제 완료", HttpStatus.OK);
    }

    // 배너 수정
    @PostMapping("/admin/banners/{bannerId}/modify")
    public String bannerUpdate(@Valid BannnerFormDto bannnerFormDto, BindingResult bindingResult, @RequestParam("bannerImgFile") MultipartFile bannerImgFile, Model model){
        if(bindingResult.hasErrors()){
            return "banner/bannerForm";
        }
        if(bannerImgFile.isEmpty()&&bannnerFormDto.getId() == null){
            model.addAttribute("errorMessage", "배너 이미지는 필수 입력 값입니다.");
            return "banner/bannerForm";
        }
        try{
            bannerService.updateBanner(bannnerFormDto, bannerImgFile);
        }
        catch(Exception e){
            e.printStackTrace();
            model.addAttribute("errorMessage", "배너 수정 중 오류가 발생했습니다.");
            return "banner/bannerForm";
        }
        return "redirect:/admin/banners";
    }

    // 배너 수정 페이지
    @GetMapping("/admin/banners/{bannerId}/modify")
    public String bannerModifyForm(@PathVariable("bannerId") Long bannerId, Model model) {
        try {
            BannnerFormDto bannnerFormDto = bannerService.getBannerDtl(bannerId);
            model.addAttribute("bannnerFormDto", bannnerFormDto);
            return "banner/bannerForm";
        } catch(EntityNotFoundException e) {
            model.addAttribute("errorMessage", "존재하지 않는 배너입니다.");
            return "banner/bannerList";
        }
    }
}
