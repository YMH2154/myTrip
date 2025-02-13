package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.ItemSellStatus;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.constant.TravelType;
import com.soloProject.myTrip.constant.DomesticCategory;
import com.soloProject.myTrip.constant.OverseasCategory;
import com.soloProject.myTrip.constant.ThemeCategory;
import com.soloProject.myTrip.entity.Schedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ItemFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemName;

    @NotNull(message = "여행 타입은 필수 선택 값입니다.")
    private TravelType travelType;

    private DomesticCategory domesticCategory;
    private OverseasCategory overseasCategory;
    private ThemeCategory themeCategory;

    @NotBlank(message = "여행 목적지는 필수 입력 값입니다.")
    private String destination;

    @NotNull(message = "여행 기간은 필수 입력 값입니다.")
    private Integer duration;

    //예약 가능 날짜
    private List<LocalDate> availableDates;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    @NotBlank(message = "상품 상세설명은 필수 입력 값입니다.")
    private String itemDetail;

    private ItemSellStatus itemSellStatus;

    @NotNull(message = "잔여 좌석(최대 인원)은 필수 입력 값입니다.")
    private Integer remainingSeats;

    @NotNull(message = "최소 출발 인원은 필수 입력 값입니다.")
    private Integer minParticipants;

    private List<String> itemImageUrls;

    private List<ScheduleDto> scheduleDtos;

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