package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.entity.Schedule;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import java.util.List;

@Getter
@Setter
public class ScheduleDto {
    private Long id;
    private Long itemId;
    private Integer day;
    private List<String> activity;
    private List<String> imageUrl;
    private List<String> description;

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
