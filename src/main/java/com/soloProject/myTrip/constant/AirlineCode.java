package com.soloProject.myTrip.constant;

import lombok.AllArgsConstructor;

@AllArgsConstructor
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
    TWAY("TW", "티웨이 항공"),
    AIRPREMIA("YP", "에어 프레미아"),
    XIAMEN("MF", "샤먼 항공"),
    FRANCE("AF","에어 프랑스"),
    HK("HX","홍콩 에어라인"),
    TURKISH("TK","터키 항공"),
    AUSTRIAN("OS","오스트리아 항공"),
    HANSA("LH","루프트 한자"),
    MONGOL("OM","몽골 항공"),
    CHINA("CA","중국 국제 항공"),
    EY("EY","에티하드 항공");



    private final String code;
    private final String companyName;

    public String getCode(){
        return code;
    }

    public String getCompanyName(){
        return companyName;
    }

    public static String getCompanyNameByCode(String code) {
        for (AirlineCode airline : values()) {
            if (airline.getCode().equals(code)) {
                return airline.getCompanyName();
            }
        }
        return null;  // 없으면 null 반환
    }

}
