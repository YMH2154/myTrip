package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ScheduleDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Schedule;
import com.soloProject.myTrip.repository.ItemRepository;
import com.soloProject.myTrip.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ScheduleService {

    private final ItemRepository itemRepository;
    private final ScheduleRepository scheduleRepository;
    private final FileService fileService;

    @Value("${activityImageLocation}")
    private String activityImageLocation;

    // 여행 일정 등록
    public void saveSchedule(Long itemId, int day, List<String> activities,
            List<MultipartFile> imageFiles, List<String> descriptions) throws Exception {
        log.info("일정 저장 시작 - 상품 ID: {}, 일차: {}", itemId, day);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        for (int i = 0; i < activities.size(); i++) {
            Schedule schedule = new Schedule();
            schedule.setItem(item);
            schedule.setDay(day);
            schedule.setActivity(activities.get(i));
            schedule.setDescription(descriptions.get(i));

            if (!imageFiles.get(i).isEmpty()) {
                String imageUrl = fileService.saveImageFile(imageFiles.get(i), "activity");
                schedule.setImageUrl(imageUrl);
                log.info("이미지 저장 완료 - URL: {}", imageUrl);
            }

            scheduleRepository.save(schedule);
            log.info("일정 저장 완료 - 활동: {}", activities.get(i));
        }
    }

    // 여행 일정 업데이트
    public void updateSchedule(Long itemId, int day, List<String> activities,
            List<MultipartFile> imageFiles,
            List<String> existingImageUrls,
            List<String> descriptions) throws Exception {

        // 해당 일차의 기존 일정들 조회
        List<Schedule> existingSchedules = scheduleRepository.findByItemIdAndDay(itemId, day);

        // 기존 이미지 URL들을 저장
        List<String> oldImageUrls = existingSchedules.stream()
                .map(Schedule::getImageUrl)
                .filter(url -> url != null && !url.isEmpty())
                .collect(Collectors.toList());

        // 기존 일정들 삭제
        scheduleRepository.deleteAll(existingSchedules);

        // 새로운 일정들을 저장
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        for (int i = 0; i < activities.size(); i++) {
            Schedule schedule = new Schedule();
            schedule.setItem(item);
            schedule.setDay(day);
            schedule.setActivity(activities.get(i));
            schedule.setDescription(descriptions.get(i));

            // 이미지 처리
            MultipartFile imageFile = imageFiles.get(i);
            if (!imageFile.isEmpty()) {
                // 새로운 이미지가 업로드된 경우
                String newImageUrl = fileService.saveImageFile(imageFile, "schedule");
                schedule.setImageUrl(newImageUrl);

                // 기존 이미지가 있었다면 삭제
                if (i < oldImageUrls.size()) {
                    String oldImageUrl = oldImageUrls.get(i);
                    if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                        deleteScheduleImageFile(oldImageUrl);
                    }
                }
            } else if (i < existingImageUrls.size() && !existingImageUrls.get(i).isEmpty()) {
                // 기존 이미지를 유지하는 경우
                schedule.setImageUrl(existingImageUrls.get(i));
                // 기존 이미지 URL을 oldImageUrls에서 제거 (삭제하지 않을 이미지)
                oldImageUrls.remove(existingImageUrls.get(i));
            }

            scheduleRepository.save(schedule);
        }

        // 사용되지 않는 나머지 기존 이미지들 삭제
        for (String remainingOldUrl : oldImageUrls) {
            if (remainingOldUrl != null && !remainingOldUrl.isEmpty()) {
                deleteScheduleImageFile(remainingOldUrl);
            }
        }
    }

    // 여행 일정 조회
    @Transactional(readOnly = true)
    public List<ScheduleDto> getScheduleDtl(Long itemId) {
        log.info("일정 조회 시작 - 상품 ID: {}", itemId);

        List<Schedule> scheduleList = scheduleRepository.findByItemId(itemId);
        log.info("조회된 일정 수: {}", scheduleList.size());

        List<ScheduleDto> scheduleDtos = new ArrayList<>();
        for (Schedule schedule : scheduleList) {
            ScheduleDto dto = ScheduleDto.of(schedule);
            log.info("일정 변환 - 일차: {}, 활동: {}, 이미지 URL: {}",
                    dto.getDay(), dto.getActivity(), dto.getImageUrl());
            scheduleDtos.add(dto);
        }

        return scheduleDtos;
    }

    // 특정 상품의 모든 일정 삭제
    public void deleteAllSchedulesByItemId(Long itemId) throws Exception {
        List<Schedule> schedules = scheduleRepository.findByItemId(itemId);

        for (Schedule schedule : schedules) {
            try {
                if (schedule.getImageUrl() != null && !schedule.getImageUrl().isEmpty()) {
                    String fileName = schedule.getImageUrl().substring(
                            schedule.getImageUrl().lastIndexOf("/") + 1);
                    String filePath = activityImageLocation + "/" + fileName;

                    System.out.println("Attempting to delete file: " + filePath);
                    fileService.deleteFile(filePath);
                    System.out.println("File deleted successfully: " + filePath);
                }
            } catch (Exception e) {
                System.out.println("Error deleting file: " + schedule.getImageUrl() + " - " + e.getMessage());
            }
        }

        scheduleRepository.deleteAll(schedules);
    }

    // 여행 일정 삭제
    public void deleteSchedule(Long scheduleId) throws Exception {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));

        try {
            // 이미지 파일 삭제
            if (schedule.getImageUrl() != null && !schedule.getImageUrl().isEmpty()) {
                // 이미지 URL에서 파일명만 추출 (예: http://localhost:8080/images/activity/filename.jpg ->
                // filename.jpg)
                String fileName = schedule.getImageUrl().substring(
                        schedule.getImageUrl().lastIndexOf("/") + 1);

                // 실제 파일 경로 구성
                String filePath = activityImageLocation + "/" + fileName;

                // 파일 삭제 시도 및 로깅
                System.out.println("Attempting to delete file: " + filePath);
                fileService.deleteFile(filePath);
                System.out.println("File deleted successfully: " + filePath);
            }

            // DB에서 일정 삭제
            scheduleRepository.delete(schedule);

        } catch (Exception e) {
            System.out.println("Error deleting schedule: " + e.getMessage());
            throw new Exception("일정 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void deleteScheduleImageFile(String imageUrl) throws Exception {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        String filePath = activityImageLocation + "/" + fileName;
        fileService.deleteFile(filePath);
    }
}
