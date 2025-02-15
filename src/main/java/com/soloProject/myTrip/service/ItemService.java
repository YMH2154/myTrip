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
public class ItemService {

    private final ItemRepository itemRepository;
    private final ScheduleRepository scheduleRepository;
    private final FileService fileService;

    @Value("${itemImageLocation}")
    private String itemImageLocation;

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
        item.setItemImageUrls(itemImageUrlList);
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
    public void deleteItemImage(Long itemId, int index) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        List<String> imageUrls = item.getItemImageUrls();
        if (index >= 0 && index < imageUrls.size()) {
            imageUrls.remove(index);
            item.setItemImageUrls(imageUrls);
        }
    }

    // 아이템 페이지 조회
    @Transactional(readOnly = true)
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    //여행 상품 삭제
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        itemRepository.delete(item);
    }

    // 상품 이미지 저장
    private String saveItemImageFile(MultipartFile imageFile) throws Exception{
        String oriThumbnailName = imageFile.getOriginalFilename(); // 본래 오리지널 썸네일 경로
        String imageUrl = "";
        if (!StringUtils.isEmpty(oriThumbnailName)){
            String fileName = fileService.uploadFile(itemImageLocation, oriThumbnailName, imageFile.getBytes());
            imageUrl = "/itemImages/"+fileName;
        }
        return imageUrl;
    }

}
