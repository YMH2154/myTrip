package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.DomesticCategory;
import com.soloProject.myTrip.constant.OverseasCategory;
import com.soloProject.myTrip.constant.ThemeCategory;
import com.soloProject.myTrip.constant.TravelType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSearchDto {
    private String searchDateType; // 등록일 기준

    private TravelType travelType; // 여행 종류

    private DomesticCategory domesticCategory;

    private OverseasCategory overseasCategory;

    private ThemeCategory themeCategory;

    private String searchBy; // 조회 유형 ( 제목 / 등록자 )

    private String searchQuery = ""; //검색 단어
}
