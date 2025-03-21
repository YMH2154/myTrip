package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.*;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.Schedule;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ItemFormDto {

    private Long id;

    @NotEmpty(message = "상품명은 필수 입력 값입니다.")
    @Length(max = 50, message = "50자 이하로 입력해주세요")
    private String itemName;

    @NotNull(message = "여행 타입은 필수 선택 값입니다.")
    private TravelType travelType;

    private DomesticCategory domesticCategory;
    private OverseasCategory overseasCategory;
    private ThemeCategory themeCategory;
    private AirportCode origin;
    private AirportCode destination;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    private String itemDetailImageUrl;

    @NotNull(message = "잔여 좌석(최대 인원)은 필수 입력 값입니다.")
    private Integer maxParticipants;

    @NotNull(message = "최소 출발 인원은 필수 입력 값입니다.")
    private Integer minParticipants;

    private Integer currentParticipants;

    private List<String> thumbnailImageUrls = new ArrayList<>();

    private List<ScheduleDto> scheduleDtos;

    private Integer night;
    private Integer duration;

    private boolean hasLeader; // 인솔자 유무
    private boolean hasGuideFee; // 가이드 경비 유무
    private Integer guideFee; // 가이드 경비 금액
    private CurrencyUnit guideFeeUnit; // 가이드 경비 통화 단위
    private boolean hasShopping; // 쇼핑 유무
    private Integer shoppingCount; // 쇼핑 횟수
    private boolean hasInsurance; // 여행자 보험 유무

    private Integer reservationCount = 0;

    private List<Schedule> schedules = new ArrayList<>();
    private LocalDate earliestDepartureDate;
    private Integer lowestPrice;

    public static ModelMapper modelMapper = new ModelMapper();

    public boolean isValidCategory() {
        if (travelType == null)
            return true;

        switch (travelType) {
            case DOMESTIC -> {
                return domesticCategory == null;
            }
            case OVERSEAS -> {
                return overseasCategory == null;
            }
            case THEME -> {
                return themeCategory == null;
            }
            default -> {
                return true;
            }
        }
    }

    // Item 엔티티로 변환하는 메소드
    public Item createItem() {
        return modelMapper.map(this, Item.class);
    }

    // Dto로 변환하는 메소드
    public static ItemFormDto of(Item item) {
        return modelMapper.map(item, ItemFormDto.class);
    }
}