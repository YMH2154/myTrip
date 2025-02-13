package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ItemSearchDto;
import com.soloProject.myTrip.dto.ScheduleDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Schedule;
import com.soloProject.myTrip.repository.ItemRepository;

import com.soloProject.myTrip.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Primary
public class ItemService {

    private final ItemRepository itemRepository;
    private final ScheduleRepository scheduleRepository;

    @Value("${itemImageLocation}")
    private String itemImageLocation;

    @Value("${activityImageLocation}")
    private String activityImageLocation;

    // 여행 상품 등록
    public void saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImages) throws Exception {
        Item item = itemFormDto.createItem();
        List<String> itemImageUrlList = new ArrayList<>();
        for (MultipartFile itemImage : itemImages) {
            itemImageUrlList.add(uploadFile(itemImageLocation, itemImage.getOriginalFilename(), itemImage.getBytes()));
        }
        item.setItemImageUrls(itemImageUrlList);
        itemRepository.save(item);
    }

    // 여행 상품 업데이트
    @Transactional
    public void updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImageFiles) throws Exception {
        // 상품 찾기
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        // 상품 기본 정보 업데이트
        item.updateItem(itemFormDto);

        // 새로운 이미지가 업로드된 경우에만 이미지 처리
        List<String> updatedImageUrls = new ArrayList<>(item.getItemImageUrls()); // 기존 이미지 URL 유지

        for (MultipartFile file : itemImageFiles) {
            if (!file.isEmpty()) {
                String imageUrl = uploadFile(itemImageLocation, file.getOriginalFilename(), file.getBytes());
                updatedImageUrls.add(imageUrl);
            }
        }

        item.setItemImageUrls(updatedImageUrls);
    }

    // 여행 일정 등록
    public void saveSchedule(Long itemId, int day, List<String> activities,
            List<MultipartFile> imageFiles, List<String> descriptions) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        for (int i = 0; i < activities.size(); i++) {
            Schedule schedule = new Schedule();
            schedule.setItem(item);
            schedule.setDay(day);
            schedule.setActivity(activities.get(i));
            schedule.setDescription(descriptions.get(i));

            // 이미지 업로드 및 URL 저장
            String imageUrl = uploadFile(activityImageLocation,
                    imageFiles.get(i).getOriginalFilename(),
                    imageFiles.get(i).getBytes());
            schedule.setImageUrl(imageUrl);

            scheduleRepository.save(schedule);
        }
    }

    // 여행 상품 데이터 조회
    @Transactional(readOnly = true)
    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
    }

    // 파일 업로드
    private String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception {
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        fos.write(fileData);
        fos.close();

        return fileUploadFullUrl;
    }

    @Transactional(readOnly = true)
    public List<Item> getDeadlineItems() {
        PageRequest pageRequest = PageRequest.of(0, 8);
        return itemRepository.findTop8ByOrderByRemainingSeatsAsc(pageRequest);
    }

    @Transactional(readOnly = true)
    public List<Item> getConfirmedItems() {
        PageRequest pageRequest = PageRequest.of(0, 8);
        return itemRepository.findTop8ByCurrentParticipantsGreaterThanEqualMinParticipants(pageRequest);
    }

    // 상세 페이지 조회
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        List<Schedule> schedules = scheduleRepository.findByItemId(itemId);
        List<ScheduleDto> scheduleDtoList = new ArrayList<>();
        for (Schedule schedule : schedules) {
            scheduleDtoList.add(ScheduleDto.of(schedule));
        }
        itemFormDto.setScheduleDtos(scheduleDtoList);

        return itemFormDto;
    }

    // 여행 일정 수정
    public void updateSchedule(Long scheduleId, int day, String activity,
            String description, MultipartFile imageFile) throws Exception {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));

        schedule.setDay(day);
        schedule.setActivity(activity);
        schedule.setDescription(description);

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = uploadFile(activityImageLocation,
                    imageFile.getOriginalFilename(),
                    imageFile.getBytes());
            schedule.setImageUrl(imageUrl);
        }
    }

    // 여행 일정 삭제
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("일정을 찾을 수 없습니다."));
        scheduleRepository.delete(schedule);
    }

    public List<Schedule> getSchedulesByItemId(Long itemId) {
        return scheduleRepository.findByItemId(itemId);
    }

    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        itemRepository.delete(item);
    }

    @Transactional
    public void deleteItemImage(Long itemId, int index) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        List<String> imageUrls = item.getItemImageUrls();
        if (index >= 0 && index < imageUrls.size()) {
            imageUrls.remove(index);
            item.setItemImageUrls(imageUrls);
        }
    }

    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }
}