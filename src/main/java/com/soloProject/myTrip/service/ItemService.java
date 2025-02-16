package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ItemSearchDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Schedule;
import com.soloProject.myTrip.repository.ItemRepository;
import com.soloProject.myTrip.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ScheduleRepository scheduleRepository;
    private final FileService fileService;

    @Value("${itemImageLocation}")
    private String itemImageLocation;

    @Value("${activityImageLocation}")
    private String activityImageLocation;

    // 여행 상품 등록
    public void saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImages) throws Exception {
        Item item = itemFormDto.createItem();
        List<String> itemImageUrlList = new ArrayList<>();
        for (MultipartFile itemImage : itemImages) {
            itemImageUrlList.add(saveItemImageFile(itemImage));
        }
        item.setItemImageUrls(itemImageUrlList);
        itemRepository.save(item);
    }

    // 여행 상품 업데이트
    public void updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImages) throws Exception {
        // 상품 찾기
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        List<String> itemImageUrlList = new ArrayList<>();

        for (MultipartFile itemImage : itemImages) {
            itemImageUrlList.add(saveItemImageFile(itemImage));
        }
        // 상품 기본 정보 업데이트
        item.updateItem(itemFormDto);
        item.setItemImageUrls(updateItemImageFile(item.getId(), itemImages));
    }

    // 상품 데이터 조회
    @Transactional(readOnly = true)
    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
    }

    // 상품 조회
    @Transactional(readOnly = true)
    public ItemFormDto getItemDtl(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        return ItemFormDto.of(item);
    }

    // 상품 이미지 삭제
    @Transactional
    public void deleteItemImage(Long itemId, int index) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        List<String> imageUrls = item.getItemImageUrls();
        if (index >= 0 && index < imageUrls.size()) {
            String savedFileUrl = imageUrls.get(index);
            deleteItemImageFile(savedFileUrl);
            imageUrls.remove(index);
            item.setItemImageUrls(imageUrls);
        }
    }

    // 아이템 페이지 조회
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    // 여행 상품 삭제
    public void deleteItem(Long itemId) throws Exception {
        // 상품 엔티티 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        try {
            // 1. 상품 이미지 파일 삭제
            List<String> itemImgs = itemRepository.findById(itemId)
                    .orElseThrow(EntityNotFoundException::new)
                    .getItemImageUrls();
            for (String itemImg : itemImgs) {
                if (itemImg != null && !itemImg.isEmpty()) {
                    String imgPath = itemImageLocation + "/" +
                            itemImg.substring(itemImg.lastIndexOf("/") + 1);
                    fileService.deleteFile(imgPath);
                }
            }

            // 2. 일정 관련 이미지 파일 삭제
            List<Schedule> schedules = scheduleRepository.findByItemId(itemId);
            for (Schedule schedule : schedules) {
                if (schedule.getImageUrl() != null && !schedule.getImageUrl().isEmpty()) {
                    String imgPath = activityImageLocation + "/" +
                            schedule.getImageUrl().substring(schedule.getImageUrl().lastIndexOf("/") + 1);
                    fileService.deleteFile(imgPath);
                }
            }

            // 3. 데이터베이스에서 삭제 (CascadeType.REMOVE로 연관 엔티티도 함께 삭제)
            itemRepository.delete(item);

        } catch (Exception e) {
            throw new Exception("상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 상품 이미지 저장
    private String saveItemImageFile(MultipartFile imageFile) throws Exception {
        String oriFilename = imageFile.getOriginalFilename(); // 본래 오리지널 썸네일 경로
        String imageUrl = "";
        if (!StringUtils.isEmpty(oriFilename)) {
            String fileName = fileService.uploadFile(itemImageLocation, oriFilename, imageFile.getBytes());
            imageUrl = "/itemImages/" + fileName;
        }
        return imageUrl;
    }

    // 상품 이미지 업데이트
    private List<String> updateItemImageFile(Long itemId, List<MultipartFile> imageFiles) throws Exception {
        List<String> savedFileUrls = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new)
                .getItemImageUrls();
        List<String> newFileUrls = new ArrayList<>();
        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile imageFile = imageFiles.get(i);
            String savedFileUrl = savedFileUrls.get(i);
            if (!imageFile.isEmpty() && !StringUtils.isEmpty(savedFileUrl)) {
                deleteItemImageFile(savedFileUrl);
            }
            String oriFilename = imageFile.getOriginalFilename();
            String fileName = fileService.uploadFile(itemImageLocation, oriFilename, imageFile.getBytes());
            newFileUrls.add(fileName);
        }
        return newFileUrls;
    }

    // 상품 이미지 삭제
    private void deleteItemImageFile(String fileUrl) throws Exception {
        String savedFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        fileService.deleteFile(itemImageLocation + "/" + savedFileName);
    }

}
