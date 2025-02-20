package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ScheduleDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Schedule;
import com.soloProject.myTrip.service.ItemService;
import com.soloProject.myTrip.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

    private final ItemService itemService;
    private final ScheduleService scheduleService;

    // 여행 일정 등록페이지(GET)
    @GetMapping("/admin/item/{itemId}/schedule")
    public String newSchedule(Model model, @PathVariable("itemId") Long itemId) {
        ItemFormDto item = itemService.getItem(itemId);
        List<ScheduleDto> scheduleDtoList = scheduleService.getScheduleDtl(itemId);
        model.addAttribute("item", item);
        model.addAttribute("scheduleDtoList", scheduleDtoList);
        return "item/itemScheduleForm";
    }

    // 여행 일정 등록(POST)
    @PostMapping("/admin/item/{itemId}/schedule/new")
    public String newSchedule(@PathVariable("itemId") Long itemId,
            @RequestParam("days") List<Integer> days,
            @RequestParam("activity") List<String> activity,
            @RequestParam("imageFile") List<MultipartFile> imageFile,
            @RequestParam("description") List<String> description,
            Model model) {

        try {
            // 각 일차별로 데이터를 그룹화
            Map<Integer, List<Integer>> dayIndices = new HashMap<>(); // 각 일차별(Integer)로 인덱스 리스트(List<Integer>)를 저장하기 위한
                                                                      // Map

            // 각 일차별 데이터 인덱스 그룹화
            for (int i = 0; i < days.size(); i++) {
                int day = days.get(i); // 일차를 day에 대입 (ex. 1,1,2,2,3...)
                dayIndices.computeIfAbsent(day, k -> new ArrayList<>()).add(i); // dayIndices에 해당하는 key가 없으면 List 초기화,
                                                                                // 있으면 해당 List 호출 후 index를 추가
            }

            // 각 일차별로 데이터 저장
            for (Map.Entry<Integer, List<Integer>> entry : dayIndices.entrySet()) { // dayIndices를 반복문
                int day = entry.getKey(); // key를 변수에 대입
                List<Integer> indices = entry.getValue(); // value(인덱스 List)를 변수에 대입

                // 각 일차 별 인덱스의 값으로 해당 일차의 일정 List를 저장
                List<String> dayActivities = indices.stream()
                        .map(activity::get)
                        .toList();

                List<MultipartFile> dayImageFiles = indices.stream()
                        .map(imageFile::get)
                        .toList();

                List<String> dayDescriptions = indices.stream()
                        .map(description::get)
                        .toList();

                scheduleService.saveSchedule(itemId, day, dayActivities, dayImageFiles, dayDescriptions);
            }
            return "redirect:/admin/items";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "에러 발생 콘솔 확인");
            return "item/itemScheduleForm";
        }
    }

    // 여행 일정 수정(POST)
    @PostMapping("/admin/item/{itemId}/schedule")
    public String updateSchedule(@PathVariable("itemId") Long itemId,
            @RequestParam(value = "days", required = false) List<Integer> days,
            @RequestParam(value = "activity", required = false) List<String> activities,
            @RequestParam(value = "imageFile", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "existingImageUrl", required = false) List<String> existingImageUrls,
            @RequestParam(value = "description", required = false) List<String> descriptions,
            Model model) {
        try {
            // 모든 일정이 삭제된 경우
            if (days == null || days.isEmpty()) {
                // 기존 일정 모두 삭제
                scheduleService.deleteAllSchedulesByItemId(itemId);
                return "redirect:/admin/items";
            }

            // 기존 로직
            Map<Integer, List<Integer>> dayIndices = new HashMap<>();
            for (int i = 0; i < days.size(); i++) {
                dayIndices.computeIfAbsent(days.get(i), k -> new ArrayList<>()).add(i);
            }

            // 각 일차별로 데이터 처리
            for (Map.Entry<Integer, List<Integer>> entry : dayIndices.entrySet()) {
                int day = entry.getKey();
                List<Integer> indices = entry.getValue();

                // 해당 일차의 데이터만 추출
                List<String> dayActivities = indices.stream()
                        .map(activities::get)
                        .toList();

                List<MultipartFile> dayImageFiles = indices.stream()
                        .map(imageFiles::get)
                        .toList();

                List<String> dayExistingImageUrls = existingImageUrls != null ? indices.stream()
                        .filter(i -> i < existingImageUrls.size())
                        .map(existingImageUrls::get)
                        .toList() : new ArrayList<>();

                List<String> dayDescriptions = indices.stream()
                        .map(descriptions::get)
                        .toList();

                scheduleService.updateSchedule(itemId, day, dayActivities,
                        dayImageFiles, dayExistingImageUrls, dayDescriptions);
            }

            return "redirect:/admin/items";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "일정 수정 중 에러가 발생했습니다: " + e.getMessage());
            return "item/itemScheduleForm";
        }
    }

    // 여행 일정 삭제(ajax)
    @DeleteMapping("/admin/item/schedule/{scheduleId}")
    @ResponseBody
    public ResponseEntity<String> deleteSchedule(@PathVariable("scheduleId") Long scheduleId) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            return new ResponseEntity<>("Success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
