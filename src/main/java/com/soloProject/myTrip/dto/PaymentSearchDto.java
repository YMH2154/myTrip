package com.soloProject.myTrip.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSearchDto {
    private String searchBy; // 검색 조건 (email, itemName, merchantUid)
    private String searchQuery; // 검색어
}
