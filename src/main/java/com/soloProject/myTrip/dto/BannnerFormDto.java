package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.entity.Banners;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class BannnerFormDto {
    
    private Long id;
    @NotEmpty(message = "배너 제목은 필수 입력 값입니다.")
    private String title;
    @NotEmpty(message = "설명은 필수 입력 값입니다.")
    private String description;

    private String locationUrl;

    private String bannerImgUrl;
    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    public static ModelMapper modelMapper = new ModelMapper();


    
    public static BannnerFormDto of(Banners banners) {
        return modelMapper.map(banners, BannnerFormDto.class);
    }
    
    public Banners createBanner() {
        return modelMapper.map(this, Banners.class);
    }
}
