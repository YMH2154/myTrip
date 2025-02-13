package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScheduleDto {
    private Long id;
    private Long itemId;
    private int day;
    private List<TouristAttractionDto> touristAttractionDtoList;

    public ScheduleDto(Long itemId){
        this.itemId = itemId;
    }
}
