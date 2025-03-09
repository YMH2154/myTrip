package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ItemSearchDto;
import com.soloProject.myTrip.entity.Item;
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
        log.info("상품 업데이트 서비스 시작 - 상품 ID: {}", itemFormDto.getId());

        // 상품 찾기
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        log.info("상품 조회 성공 : {}", item.getId());

        // 상품 기본 정보 업데이트
        log.info("상품 기본 정보 업데이트 시작");
        item.updateItem(itemFormDto);
        log.info("상품 기본 정보 업데이트 완료");

        // 썸네일 이미지 업데이트 (새로운 이미지가 있는 경우에만)
        if (!thumbnailImages.stream().allMatch(MultipartFile::isEmpty)) {
            log.info("썸네일 이미지 업데이트 시작 - 이미지 개수: {}", thumbnailImages.size());
            List<String> updatedThumbnailUrls = updateThumbnailImageFile(item.getId(), thumbnailImages);
            if (!updatedThumbnailUrls.isEmpty()) {
                item.setThumbnailImageUrls(updatedThumbnailUrls);
                log.info("썸네일 이미지 업데이트 완료 - 업데이트된 URL 개수: {}", updatedThumbnailUrls.size());
            }
        } else {
            log.info("새로운 썸네일 이미지가 없어 기존 이미지를 유지합니다.");
        }

        // 상세 이미지 업데이트 (새로운 이미지가 있는 경우에만)
        if (!itemDetailImage.isEmpty()) {
            log.info("상세 이미지 업데이트 시작");
            String updatedDetailImageUrl = fileService.updateImgFile(item.getId(), itemDetailImage, "detail");
            item.setItemDetailImageUrl(updatedDetailImageUrl);
            log.info("상세 이미지 업데이트 완료 - 새 URL: {}", updatedDetailImageUrl);
        } else {
            log.info("새로운 상세 이미지가 없어 기존 이미지를 유지합니다.");
        }

        log.info("상품 업데이트 완료");
    }

    // 상품 데이터 조회
    @Transactional(readOnly = true)
    public ItemFormDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        return ItemFormDto.of(item);
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

    // 썸네일 이미지 업데이트
    private List<String> updateThumbnailImageFile(Long itemId, List<MultipartFile> imageFiles) throws Exception {
        log.info("썸네일 이미지 업데이트 시작 - 상품 ID: {}", itemId);

        List<String> savedFileUrls = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new)
                .getThumbnailImageUrls();
        log.info("기존 썸네일 이미지 URL 개수: {}", savedFileUrls.size());

        List<String> newFileUrls = new ArrayList<>();

        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile imageFile = imageFiles.get(i);
            if (!imageFile.isEmpty()) {
                log.info("이미지 파일 처리 중 - 인덱스: {}, 파일명: {}", i, imageFile.getOriginalFilename());

                // 기존 이미지가 있다면 삭제
                if (i < savedFileUrls.size()) {
                    String savedFileUrl = savedFileUrls.get(i);
                    if (!StringUtils.isEmpty(savedFileUrl)) {
                        log.info("기존 이미지 삭제 - URL: {}", savedFileUrl);
                        deleteItemImageFile(savedFileUrl);
                    }
                }

                // 새 이미지 업로드
                String fileName = fileService.saveImageFile(imageFile, "thumbnail");
                newFileUrls.add(fileName);
                log.info("새 이미지 업로드 완료 - 파일명: {}", fileName);
            } else if (i < savedFileUrls.size()) {
                // 새 이미지가 없고 기존 이미지가 있다면 기존 이미지 유지
                newFileUrls.add(savedFileUrls.get(i));
                log.info("기존 이미지 유지 - 인덱스: {}", i);
            }
        }

        log.info("썸네일 이미지 업데이트 완료 - 새로운 URL 개수: {}", newFileUrls.size());
        return newFileUrls;
    }

    // 상품 이미지 삭제
    private void deleteItemImageFile(String fileUrl) throws Exception {
        String savedFileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        fileService.deleteFile(thumbnailImageLocation + "/" + savedFileName);
        log.info("썸네일 이미지 삭제 완료");
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

}
