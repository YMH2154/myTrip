package com.soloProject.myTrip.dto;

import com.soloProject.myTrip.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.util.Map;
import java.util.HashMap;

@Getter
@Setter
@Builder
public class ItemReservationDto {
    private int minPrice;
    private int maxPrice;

    // 날짜별 데이터
    private Map<String, Integer> remainingSeats; // 날짜별 잔여석
    private Map<String, Integer> prices; // 날짜별 가격
    private Map<String, ItemSellStatus> itemSellStatus; // 날짜별 판매상태

    // 항공편 정보
    private Map<String, FlightInfo> departureFlights; // 출발 항공편
    private Map<String, FlightInfo> returnFlights; // 귀국 항공편

    // 출발 확정 정보
    private Map<String, Boolean> departureConfirmed;

    // 출발 확정된 날짜의 departureDateTime
    private String departureDateTime;

    @Getter
    @Setter
    @Builder
    public static class FlightInfo {
        private String carrierName; // 항공사명
        private String flightNumber; // 항공편명
        private String dateTime; // 출발/도착 시간
    }

    public static ItemReservationDto createEmpty() {
        return ItemReservationDto.builder()
                .minPrice(0)
                .maxPrice(0)
                .departureConfirmed(new HashMap<>())
                .remainingSeats(new HashMap<>())
                .prices(new HashMap<>())
                .itemSellStatus(new HashMap<>())
                .departureFlights(new HashMap<>())
                .returnFlights(new HashMap<>())
                .build();
    }
}
