package com.soloProject.myTrip.service;

import com.soloProject.myTrip.dto.ItemFormDto;
import com.soloProject.myTrip.dto.ScheduleDto;
import com.soloProject.myTrip.dto.TouristAttractionDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.TouristAttraction;
import com.soloProject.myTrip.repository.ItemImageRepository;
import com.soloProject.myTrip.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;

    @Value("${imageLocation}")
    private String imageLocation;

    // 상품 등록
    public void saveItem(ItemFormDto itemFormDto)
            throws Exception {

        Item item = itemFormDto.createItem();
        itemRepository.save(item);
    }

    // 상품 조회
    public Item getItem(Long itemId){
        return itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
    }

    // 일정 등록
    public void saveSchedule(ScheduleDto scheduleDto, List<MultipartFile> imageFiles){
        // 이미지 등록
        for (int i = 0; i < imageFiles.size(); i++) {
            TouristAttraction touristAttraction = new TouristAttraction();
            touristAttraction.setItem(item);

            // 이미지 파일이 존재하고 설명이 있는 경우에만 저장
            if (!imageFiles.get(i).isEmpty() && i < descriptions.size()) {
                String originalFileName = imageFiles.get(i).getOriginalFilename();
                String imageName = uploadFile(imageLocation, originalFileName, imageFiles.get(i).getBytes());
                String imageUrl = "/image/" + imageName;

                touristAttraction.setImageUrl(imageUrl);
                // 이미지 설명이 있는 경우에만 설정
                if (imageDescriptions.get(i) != null && !imageDescriptions.get(i).trim().isEmpty()) {
                    touristAttraction.setImageDescription(imageDescriptions.get(i));
                }
                imageRepository.save(touristAttraction);
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
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        List<TouristAttraction> touristAttractions = itemImageRepository.findByItemIdOrderByIdAsc(itemId);
        List<TouristAttractionDto> touristAttractionDtos = new ArrayList<>();
        for(TouristAttraction touristAttraction : touristAttractions){
            touristAttractionDtos.add(TouristAttractionDto.of(touristAttraction));
        }
        itemFormDto.setTouristAttractionDtos(touristAttractionDtos);

        return itemFormDto;
    }
}