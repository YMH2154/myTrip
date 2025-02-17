package com.soloProject.myTrip.constant;

import org.springframework.data.domain.PageRequest;

public enum AirlineCode {
    KOREAN("KE", "대한항공"),
    ASIANA("OZ", "아시아나 항공"),
    JEJU("7C", "제주 항공"),
    JINAIR("LJ", "진에어 항공"),
    BUSAN("BX", "에어부산"),
    EASTAR("ZE", "이스타 항공"),
    INCHEON("KJ", "에어 인천"),
    SEOUL("RS", "에어 서울"),
    GANGWON("4V", "플라이 강원"),
    TWAY("TW", "티웨이 항공");

    private final String code;
    private final String companyName;

    AirlineCode(String code, String companyName){
        this.code = code;
        this.companyName = companyName;
    }

    public String getCode(){
        return code;
    }

    public String getCompanyName(){
        return companyName;
    }

}
