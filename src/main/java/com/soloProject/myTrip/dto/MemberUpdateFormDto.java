package com.soloProject.myTrip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateFormDto {

    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String tel;

}
