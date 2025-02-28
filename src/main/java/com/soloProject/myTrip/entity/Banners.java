package com.soloProject.myTrip.entity;

import com.soloProject.myTrip.dto.BannnerFormDto;
import groovy.transform.ToString;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@ToString
public class Banners extends BaseEntity{
    @Id
    @Column(name = "banner_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;  // 배너 제목
    private String description; // 배너 설명
    private String mappingUrl;
    private String bannerImgUrl;
    
    public void updateBanner(BannnerFormDto bannnerFormDto) {
        this.title = bannnerFormDto.getTitle();
        this.description = bannnerFormDto.getDescription();
        this.mappingUrl = bannnerFormDto.getMappingUrl();
    }
    
    
}
