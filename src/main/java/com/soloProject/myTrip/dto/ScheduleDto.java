package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.entity.Schedule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleDto {
    private Long id;
    private Long itemId;
    private Integer day;
    private String activity;
    private String imageUrl;
    private String description;

    public static ModelMapper modelMapper = new ModelMapper();

    public static ScheduleDto of(Schedule schedule){
        return modelMapper.map(schedule, ScheduleDto.class);
    }

    public Schedule createEntity(){
        return modelMapper.map(this, Schedule.class);
    }

    public ScheduleDto(Long itemId){
        this.itemId = itemId;
    }
}
