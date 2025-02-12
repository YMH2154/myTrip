package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemImage;
import com.soloProject.myTrip.repository.ItemImageRepository;
import com.soloProject.myTrip.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;

    @Value("${itemImageLocation}")
    private String itemImageLocation;

    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImageFiles, List<String> imageDescriptions)
            throws Exception {
        // 상품 등록
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        // 이미지 등록
        for (int i = 0; i < itemImageFiles.size(); i++) {
            ItemImage itemImage = new ItemImage();
            itemImage.setItem(item);

            // 이미지 파일이 존재하고 설명이 있는 경우에만 저장
            if (!itemImageFiles.get(i).isEmpty() && i < imageDescriptions.size()) {
                String originalFileName = itemImageFiles.get(i).getOriginalFilename();
                String imageName = uploadFile(itemImageLocation, originalFileName, itemImageFiles.get(i).getBytes());
                String imageUrl = "/itemImage/" + imageName;

                itemImage.setImageUrl(imageUrl);
                // 이미지 설명이 있는 경우에만 설정
                if (imageDescriptions.get(i) != null && !imageDescriptions.get(i).trim().isEmpty()) {
                    itemImage.setImageDescription(imageDescriptions.get(i));
                }
                itemImageRepository.save(itemImage);
            }
        }
        return item.getId();
    }

    //파일 업로드
    private String uploadFile(String uploadPath, String originalFileName, byte[] fileData) throws Exception {
        UUID uuid = UUID.randomUUID();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + "/" + savedFileName;

        FileOutputStream fos = new FileOutputStream(fileUploadFullUrl);
        fos.write(fileData);
        fos.close();

        return savedFileName;
    }

    public List<Item> getDeadlineItems() {
        PageRequest pageRequest = PageRequest.of(0, 8);
        return itemRepository.findTop8ByOrderByRemainingSeatsAsc(pageRequest);
    }

    public List<Item> getConfirmedItems() {
        PageRequest pageRequest = PageRequest.of(0, 8);
        return itemRepository.findTop8ByCurrentParticipantsGreaterThanEqualMinParticipants(pageRequest);
    }

    //상세 페이지 조회
    public ItemFormDto getItemDtl(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        List<ItemImage> itemImages = itemImageRepository.findByItemIdOrderByIdAsc(itemId);
        ItemFormDto itemFormDto =
    }
}