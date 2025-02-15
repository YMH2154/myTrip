package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ScheduleDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Schedule;
import com.soloProject.myTrip.repository.ItemRepository;
import com.soloProject.myTrip.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ItemRepository itemRepository;
    private final ScheduleRepository scheduleRepository;
    private final FileService fileService;

    @Value("${activityImageLocation}")
    private String activityImageLocation;

    // 여행 일정 등록
    public void saveSchedule(Long itemId, int day, List<String> activities,
            List<MultipartFile> imageFiles, List<String> descriptions) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        Schedule schedule = new Schedule();
        schedule.setItem(item);
        schedule.setDay(day);

        // List 형식으로 데이터 설정
        schedule.setActivity(activities);
        schedule.setDescription(descriptions);

        // 이미지 URL List 생성 및 설정
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            String imageUrl = saveScheduleImageFile(imageFile);
            imageUrls.add(imageUrl);
        }
        schedule.setImageUrl(imageUrls);

        scheduleRepository.save(schedule);
    }

    // 여행 일정 업데이트
    public void updateSchedule(Long itemId, int day, List<String> activities,
            List<MultipartFile> imageFiles,
            List<String> existingImageUrls,
            List<String> descriptions) throws Exception {

        // 기존 일정 조회
        Schedule schedule = scheduleRepository.findByItemIdAndDay(itemId, day)
                .orElseGet(() -> {
                    Schedule newSchedule = new Schedule();
                    newSchedule.setItem(itemRepository.findById(itemId)
                            .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다.")));
                    newSchedule.setDay(day);
                    return newSchedule;
                });

        // 활동과 설명 업데이트
        schedule.setActivity(activities);
        schedule.setDescription(descriptions);

        // 이미지 처리
        List<String> updatedImageUrls = new ArrayList<>();
        for (int i = 0; i < activities.size(); i++) {
            MultipartFile imageFile = imageFiles.get(i);
            if (!imageFile.isEmpty()) {
                // 새 이미지가 업로드된 경우
                String imageUrl = saveScheduleImageFile(imageFile);
                updatedImageUrls.add(imageUrl);
            } else if (i < existingImageUrls.size() && !existingImageUrls.get(i).isEmpty()) {
                // 기존 이미지를 유지하는 경우
                updatedImageUrls.add(existingImageUrls.get(i));
            }
        }
        schedule.setImageUrl(updatedImageUrls);

        scheduleRepository.save(schedule);
    }

    // 여행 일정 조회
    @Transactional(readOnly = true)
    public List<Schedule> getScheduleDtl(Long itemId) {
        return scheduleRepository.findByItemId(itemId);
    }

    // 여행 일정 수정

    // 여행 일정 삭제
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));
        scheduleRepository.delete(schedule);
    }

    private String saveScheduleImageFile(MultipartFile imageFile) throws Exception {
        String oriThumbnailName = imageFile.getOriginalFilename(); // 본래 오리지널 썸네일 경로
        String imageUrl = "";
        if (!StringUtils.isEmpty(oriThumbnailName)) {
            String fileName = fileService.uploadFile(activityImageLocation, oriThumbnailName, imageFile.getBytes());
            imageUrl = "/activityImages/" + fileName;
        }
        return imageUrl;
    }
}
