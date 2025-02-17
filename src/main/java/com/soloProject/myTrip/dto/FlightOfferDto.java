package com.soloProject.myTrip.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class FlightOfferDto {
    private String departureDate;
    private String returnDate;
    private BigDecimal price;
    private String currency;
    private String origin;
    private String destination;

    @Builder
    public FlightOfferDto(String departureDate, String returnDate,
                          BigDecimal price, String currency,
                          String origin, String destination){
        this.currency = currency;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.price = price;
        this.origin = origin;
        this. destination = destination;
    }
}
