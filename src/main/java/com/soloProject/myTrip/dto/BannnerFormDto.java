package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.entity.Banners;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

    private String mappingUrl;

    private String bannerImgUrl;
    private LocalDateTime regTime;
    private LocalDateTime updateTime;
    
    public static BannnerFormDto of(Banners banners) {
        BannnerFormDto bannnerFormDto = new BannnerFormDto();
        bannnerFormDto.setId(banners.getId());
        bannnerFormDto.setTitle(banners.getTitle());
        bannnerFormDto.setDescription(banners.getDescription());
        bannnerFormDto.setMappingUrl(banners.getMappingUrl());
        bannnerFormDto.setBannerImgUrl(banners.getBannerImgUrl());
        bannnerFormDto.setRegTime(banners.getRegTime());
        bannnerFormDto.setUpdateTime(banners.getUpdateTime());
        
        return bannnerFormDto;
    }
    
    public Banners createBanner() {
        Banners banners = new Banners();
        banners.setTitle(this.title);
        banners.setDescription(this.description);
        banners.setMappingUrl(this.mappingUrl);
        return banners;
    }
}
