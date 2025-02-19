package com.soloProject.myTrip.constant;

public enum AirportCode {
    // 대한민국 (Korea)
    ICN("ICN", "인천", "대한민국"), // 인천 공항
    GMP("GMP", "김포", "대한민국"), // 김포 공항
    PUS("PUS", "김해", "대한민국"), // 김해 공항
    CJU("CJU", "제주", "대한민국"),

    // 미국 (USA)
    JFK("JFK", "케네디", "미국"), // JFK 공항
    LAX("LAX", "LA", "미국"), // LA 공항
    ORD("ORD", "시카고", "미국"), // 시카고 공항
    ATL("ATL", "애틀랜타", "미국"), // 애틀랜타 공항

    // 일본 (Japan)
    HND("HND", "하네다", "일본"), // 하네다 공항
    NRT("NRT", "나리타", "일본"), // 나리타 공항
    KIX("KIX", "오사카 간사이", "일본"), // 간사이 국제 공항
    CTS("CTS", "홋카이도 신치토세", "일본"),

    // 유럽 (Europe)
    LHR("LHR", "런던 히드로", "영국"), // 런던 히드로 공항
    CDG("CDG", "파리 샤를 드 골", "프랑스"), // 파리 샤를 드 골 공항
    FRA("FRA", "프랑크푸르트", "독일"), // 프랑크푸르트 공항
    BCN("BCN", "바르셀로나 엘프라트", "스페인"),
    ZRH("ZRH", "취리히", "스위스"),
    IST("IST", "이스탄불", "튀르키예"),

    // 기타 주요 공항
    SYD("SYD", "시드니", "호주"), // 시드니 공항
    SIN("SIN", "싱가포르 창이", "싱가포르"), // 싱가포르 창이 공항
    BKK("BKK", "수완나품", "태국"),
    DAD("DAD", "다낭", "베트남");

    private final String code;
    private final String name;
    private final String country;

    // 생성자
    AirportCode(String code, String name, String country) {
        this.code = code;
        this.name = name;
        this.country = country;
    }

    // getter 메서드들
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    // 공항 코드로 공항 이름을 찾는 메서드
    public static String getAirportNameByCode(String code) {
        for (AirportCode airportCode : values()) {
            if (airportCode.getCode().equalsIgnoreCase(code)) {
                return airportCode.getName();
            }
        }
        return "Unknown Airport";
    }

    // 공항 코드로 도시를 찾는 메서드
    public static String getCityByCode(String code) {
        for (AirportCode airportCode : values()) {
            if (airportCode.getCode().equalsIgnoreCase(code)) {
                return airportCode.getCountry();
            }
        }
        return "Unknown City";
    }
}
