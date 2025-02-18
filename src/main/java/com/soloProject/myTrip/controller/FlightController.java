package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.dto.ErrorResponse;
import com.soloProject.myTrip.dto.FlightOfferDto;
import com.soloProject.myTrip.exception.FlightSearchException;
import com.soloProject.myTrip.service.FlightSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Slf4j
public class FlightController {

    private final FlightSearchService flightSearchService;

    @GetMapping("/search")
    public ResponseEntity<?> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate departureDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate returnDate){
        try{
            List<FlightOfferDto> flightOffers = flightSearchService.searchFlights(
                    origin, destination, departureDate, returnDate);

            if(flightOffers.isEmpty()){
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(flightOffers);
        } catch (FlightSearchException e){
            log.error("항공권 검색 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}
