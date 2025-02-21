package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.Age;
import com.soloProject.myTrip.constant.Sex;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantDto {
    private String name;
    private String birth;
    private Sex sex;
    private Age age;
    private String tel;
} 