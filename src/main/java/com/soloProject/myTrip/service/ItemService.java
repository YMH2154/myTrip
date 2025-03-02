package com.soloProject.myTrip.service;

import com.querydsl.core.BooleanBuilder;
import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ItemSearchDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.QItem;
import com.soloProject.myTrip.entity.Schedule;
import com.soloProject.myTrip.repository.ItemRepository;
import com.soloProject.myTrip.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ScheduleRepository scheduleRepository;
    private final FileService fileService;
    private final ItemReservationService itemReservationService;

    @Value("${thumbnailImageLocation}")
    private String thumbnailImageLocation;

    @Value("${itemDetailImageLocation}")
    private String itemDetailImageLocation;

    @Value("${activityImageLocation}")
    private String activityImageLocation;

    // 여행 상품 등록
    @Transactional
    public void saveItem(ItemFormDto itemFormDto, MultipartFile itemDetailImage, List<MultipartFile> thumbnailImages)
            throws Exception {
        // 상품 저장 로직
        String itemDetailImageUrl = fileService.saveImageFile(itemDetailImage, "detail");
        itemFormDto.setItemDetailImageUrl(itemDetailImageUrl);
        Item savedItem = itemRepository.save(itemFormDto.createItem());
        itemRepository.flush(); // 명시적으로 flush 호출

        // 썸네일 이미지 저장 로직
        List<String> thumbnailImageUrls = new ArrayList<>();
        for (MultipartFile thumbnailImage : thumbnailImages) {
            thumbnailImageUrls.add(fileService.saveImageFile(thumbnailImage, "thumbnail"));
        }
        savedItem.setThumbnailImageUrls(thumbnailImageUrls);
        itemRepository.flush(); // 다시 한번 flush 호출

        // 별도의 트랜잭션에서 예약 엔티티 초기화
        itemReservationService.initializeReservations(savedItem);

    }

    // 여행 상품 업데이트
    public void updateItem(ItemFormDto itemFormDto, List<MultipartFile> thumbnailImages, MultipartFile itemDetailImage)
            throws Exception {
        // 상품 찾기
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        List<String> thumbnailImageUrls = new ArrayList<>();

        for (MultipartFile thumbnailImage : thumbnailImages) {
            thumbnailImageUrls.add(fileService.saveImageFile(thumbnailImage, "thumbnail"));
        }
        // 상품 기본 정보 업데이트
        item.updateItem(itemFormDto);
        item.setThumbnailImageUrls(updateThumbnailImageFile(item.getId(), thumbnailImages));
        item.setItemDetailImageUrl(fileService.updateImgFile(item.getId(), itemDetailImage, "detail"));
    }

    // 상품 데이터 조회
    @Transactional(readOnly = true)
    public ItemFormDto getItem(Long itemId) {
        return ItemFormDto.of(itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new));
    }

    // 썸네일 이미지 삭제
    @Transactional
    public void deleteThumbnailImage(Long itemId, int index) throws Exception {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        List<String> imageUrls = item.getThumbnailImageUrls();
        if (index >= 0 && index < imageUrls.size()) {
            String savedFileUrl = imageUrls.get(index);
            deleteItemImageFile(savedFileUrl);
            imageUrls.remove(index);
            item.setThumbnailImageUrls(imageUrls);
        }
    }

    // 여행 상품 삭제
    public void deleteItem(Long itemId) throws Exception {
        // 상품 엔티티 조회
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        try {
            // 1. 썸네일 이미지 파일 삭제
            List<String> thumbnailImgs = itemRepository.findById(itemId)
                    .orElseThrow(EntityNotFoundException::new)
                    .getThumbnailImageUrls();
            for (String thumbnailImg : thumbnailImgs) {
                if (thumbnailImg != null && !thumbnailImg.isEmpty()) {
                    String thumbnailImgPath = thumbnailImageLocation + "/" +
                            thumbnailImg.substring(thumbnailImg.lastIndexOf("/") + 1);
                    fileService.deleteFile(thumbnailImgPath);
                }
            }

            // 2. 상품 상세 설명 이미지 파일 삭제
            String detailImg = itemRepository.findById(itemId)
                    .orElseThrow(EntityNotFoundException::new)
                    .getItemDetailImageUrl();
            String detailImgPath = itemDetailImageLocation + "/" +
                    detailImg.substring(detailImg.lastIndexOf("/") + 1);
            fileService.deleteFile(detailImgPath);

            // 3. 일정 관련 이미지 파일 삭제
            List<Schedule> schedules = scheduleRepository.findByItemId(itemId);
            for (Schedule schedule : schedules) {
                if (schedule.getImageUrl() != null && !schedule.getImageUrl().isEmpty()) {
                    String imgPath = activityImageLocation + "/" +
                            schedule.getImageUrl().substring(schedule.getImageUrl().lastIndexOf("/") + 1);
                    fileService.deleteFile(imgPath);
                }
            }

            // 4. 데이터베이스에서 삭제 (CascadeType.REMOVE로 연관 엔티티도 함께 삭제)
            itemRepository.delete(item);

        } catch (Exception e) {
            throw new Exception("상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 상품 상세 설명 이미지 업데이트
    public String updateItemDetailImageFile(Long itemId, MultipartFile imageFile) throws Exception {
        String savedFileUrl = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new)
                .getItemDetailImageUrl();
        if (!imageFile.isEmpty() && !StringUtils.isEmpty(savedFileUrl)) {
            fileService.deleteFile(itemDetailImageLocation + "/" + savedFileUrl);
        }
        String oriFilename = imageFile.getOriginalFilename();
        return fileService.uploadFile(itemDetailImageLocation, oriFilename, imageFile.getBytes());
    }

    // 썸네일 이미지 업데이트
    private List<String> updateThumbnailImageFile(Long itemId, List<MultipartFile> imageFiles) throws Exception {
        List<String> savedFileUrls = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new)
                .getThumbnailImageUrls();
        List<String> newFileUrls = new ArrayList<>();
        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile imageFile = imageFiles.get(i);
            String savedFileUrl = savedFileUrls.get(i);
            if (!imageFile.isEmpty() && !StringUtils.isEmpty(savedFileUrl)) {
                deleteItemImageFile(savedFileUrl);
            }
            String oriFilename = imageFile.getOriginalFilename();
            String fileName = fileService.uploadFile(thumbnailImageLocation, oriFilename, imageFile.getBytes());
            newFileUrls.add(fileName);
        }
        return newFileUrls;
    }

    // 상품 이미지 삭제
    private void deleteItemImageFile(String fileUrl) throws Exception {
        String savedFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        fileService.deleteFile(thumbnailImageLocation + "/" + savedFileName);
    }

    // 활성화된 모든 상품 조회
    public List<Item> findAllItems() {
        return itemRepository.findAll();
    }

    // 관리자 상품 (검색) 페이지 조회
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    // 메인 상품 (검색) 페이지 조회
    @Transactional(readOnly = true)
    public List<ItemFormDto> getSearchItemPage(String searchQuery, int page, int pageSize) {
        QItem qItem = QItem.item;
        BooleanBuilder builder = new BooleanBuilder();

        // 검색어가 비어있지 않은 경우에만 검색 조건 추가
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            builder.and(qItem.itemName.containsIgnoreCase(searchQuery)
                    .or(qItem.domesticCategory.stringValue().containsIgnoreCase(searchQuery))
                    .or(qItem.overseasCategory.stringValue().containsIgnoreCase(searchQuery))
                    .or(qItem.themeCategory.stringValue().containsIgnoreCase(searchQuery)));
        }

        // 페이징 처리
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "regTime"));

        // QuerydslPredicateExecutor를 사용하여 검색 실행
        Page<Item> itemPage = itemRepository.findAll(builder, pageRequest);

        // Entity를 DTO로 변환
        return itemPage.getContent().stream()
                .map(ItemFormDto::of)
                .collect(Collectors.toList());
    }

    // 메인 상품 (카테고리) 페이지 조회
    @Transactional(readOnly = true)
    public List<ItemFormDto> getItemByCategory(String link, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "regTime"));
        Page<Item> itemPage = itemRepository.findItemsByCategory(link, pageRequest);

        return itemPage.getContent().stream()
                .map(ItemFormDto::of)
                .collect(Collectors.toList());
    }

}
