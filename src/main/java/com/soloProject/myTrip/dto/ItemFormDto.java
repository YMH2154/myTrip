package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.ItemSellStatus;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.constant.TravelType;
import com.soloProject.myTrip.constant.DomesticCategory;
import com.soloProject.myTrip.constant.OverseasCategory;
import com.soloProject.myTrip.constant.ThemeCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemName;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    @NotBlank(message = "상품 상세설명은 필수 입력 값입니다.")
    private String itemDetail;

    @NotBlank(message = "여행 목적지는 필수 입력 값입니다.")
    private String destination;

    @NotNull(message = "여행 시작일은 필수 입력 값입니다.")
    private LocalDate startDate;

    @NotNull(message = "여행 종료일은 필수 입력 값입니다.")
    private LocalDate endDate;

    @NotNull(message = "최대 참가자 수는 필수 입력 값입니다.")
    private Integer maxParticipants;

    @NotNull(message = "최소 출발 인원은 필수 입력 값입니다.")
    private Integer minParticipants;

    private ItemSellStatus itemSellStatus;

    @NotNull(message = "여행 타입은 필수 선택 값입니다.")
    private TravelType travelType;

    private DomesticCategory domesticCategory;
    private OverseasCategory overseasCategory;
    private ThemeCategory themeCategory;

    private List<TouristAttractionDto> touristAttractionDtos = new ArrayList<>();

    private List<Long> itemImageIds = new ArrayList<>();

    public static ModelMapper modelMapper = new ModelMapper();

    // Item 엔티티로 변환하는 메소드
    public Item createItem() {
        Item item = modelMapper.map(this, Item.class);
        item.setCurrentParticipants(0);
        return item;
    }

    // Dto로 변환하는 메소드
    public static ItemFormDto of(Item item){
        return modelMapper.map(item, ItemFormDto.class);
    }
}