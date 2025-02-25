package com.soloProject.myTrip.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class FlightOfferDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String departureDate;
    private String returnDate;
    private BigDecimal price;
    private String currency;
    private String origin;
    private String destination;
    private String departureCarrierCode;
    private String departureFlightNumber;
    private String returnCarrierCode;
    private String returnFlightNumber;

    @Builder
    public FlightOfferDto(String departureDate, String returnDate,
            BigDecimal price, String currency,
            String origin, String destination,
            String departureCarrierCode, String departureFlightNumber,
            String returnCarrierCode, String returnFlightNumber) {
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.price = price;
        this.currency = currency;
        this.origin = origin;
        this.destination = destination;
        this.departureCarrierCode = departureCarrierCode;
        this.departureFlightNumber = departureFlightNumber;
        this.returnCarrierCode = returnCarrierCode;
        this.returnFlightNumber = returnFlightNumber;
    }
}
