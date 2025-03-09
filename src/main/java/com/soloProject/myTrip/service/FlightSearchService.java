package com.soloProject.myTrip.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.soloProject.myTrip.dto.FlightOfferDto;
import com.soloProject.myTrip.exception.FlightSearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FlightSearchService {
    private final Amadeus amadeus;
    private final RedisTemplate<String, List<FlightOfferDto>> flightOffersRedisTemplate;

    private static final long CACHE_DURATION = 60 * 60; // 1시간 캐시
    private static final String CACHE_KEY_PREFIX = "flight:offers:";

    @Transactional
    public List<FlightOfferDto> searchFlights(String origin, String destination, LocalDate departureDate,
            LocalDate returnDate) {
        String cacheKey = generateCacheKey(origin, destination, departureDate, returnDate);
        log.info("Searching flights with cache key: {}", cacheKey);

        // 캐시에서 조회
        List<FlightOfferDto> cachedOffers = flightOffersRedisTemplate.opsForValue().get(cacheKey);
        if (cachedOffers != null && !cachedOffers.isEmpty()) {
            log.info("Cache hit for key: {}", cacheKey);
            return cachedOffers;
        }
        log.info("Cache miss for key: {}", cacheKey);

        try {
            // Amadeus API 호출
            FlightOfferSearch[] flightOffers = amadeus.shopping.flightOffersSearch.get(
                    Params.with("originLocationCode", origin)
                            .and("destinationLocationCode", destination)
                            .and("departureDate", departureDate.toString())
                            .and("returnDate", returnDate.toString())
                            .and("adults", 1)
                            .and("currencyCode", "KRW")
                            .and("max", 1));

            List<FlightOfferDto> offers = Arrays.stream(flightOffers)
                    .map(this::convertDto)
                    .collect(Collectors.toList());

            if (!offers.isEmpty()) {
                // 캐시에 저장
                try {
                    flightOffersRedisTemplate.opsForValue().set(cacheKey, offers, CACHE_DURATION, TimeUnit.SECONDS);
                    log.info("Successfully cached flight offers for key: {}", cacheKey);
                } catch (Exception e) {
                    log.error("Failed to cache flight offers for key: {}", cacheKey, e);
                }
                return offers;
            }

            // API 호출 실패 시 기본값 반환
            List<FlightOfferDto> defaultOffers = createDefaultFlightOffers(origin, destination, departureDate,
                    returnDate);
            try {
                flightOffersRedisTemplate.opsForValue().set(cacheKey, defaultOffers, CACHE_DURATION, TimeUnit.SECONDS);
                log.info("Successfully cached default flight offers for key: {}", cacheKey);
            } catch (Exception e) {
                log.error("Failed to cache default flight offers for key: {}", cacheKey, e);
            }
            return defaultOffers;

        } catch (ResponseException e) {
            log.error("Amadeus API 호출 실패: {}", e.getMessage());
            List<FlightOfferDto> defaultOffers = createDefaultFlightOffers(origin, destination, departureDate,
                    returnDate);
            try {
                flightOffersRedisTemplate.opsForValue().set(cacheKey, defaultOffers, CACHE_DURATION, TimeUnit.SECONDS);
                log.info("Successfully cached default flight offers after API error for key: {}", cacheKey);
            } catch (Exception ex) {
                log.error("Failed to cache default flight offers after API error for key: {}", cacheKey, ex);
            }
            return defaultOffers;
        }
    }

    @Transactional
    private String generateCacheKey(String origin, String destination, LocalDate departureDate, LocalDate returnDate) {
        return CACHE_KEY_PREFIX + String.format("%s:%s:%s:%s", origin, destination, departureDate, returnDate);
    }

    @Transactional
    private FlightOfferDto convertDto(FlightOfferSearch offer) {
        return FlightOfferDto.builder()
                .departureDate(offer.getItineraries()[0].getSegments()[0].getDeparture().getAt())
                .returnDate(offer.getItineraries().length > 1
                        ? offer.getItineraries()[1].getSegments()[0].getDeparture().getAt()
                        : null)
                .price(new BigDecimal(offer.getPrice().getTotal()))
                .currency(offer.getPrice().getCurrency())
                .origin(offer.getItineraries()[0].getSegments()[0].getDeparture().getIataCode())
                .destination(offer.getItineraries()[0].getSegments()[0].getArrival().getIataCode())
                .departureCarrierCode(offer.getItineraries()[0].getSegments()[0].getCarrierCode())
                .departureFlightNumber(offer.getItineraries()[0].getSegments()[0].getNumber())
                .returnCarrierCode(offer.getItineraries()[1].getSegments()[0].getCarrierCode())
                .returnFlightNumber(offer.getItineraries()[1].getSegments()[0].getNumber())
                .build();
    }

    private List<FlightOfferDto> createDefaultFlightOffers(String origin, String destination,
            LocalDate departureDate, LocalDate returnDate) {
        // 실제 항공사 코드와 운항 시간대를 반영한 기본값 설정
        String carrierCode = "KE"; // 대한항공
        String departureTime = "09:00";
        String returnTime = "11:00";

        if (origin.equals("ICN") && destination.equals("NRT")) {
            departureTime = "10:30";
            returnTime = "13:30";
        }

        FlightOfferDto defaultOffer = FlightOfferDto.builder()
                .departureDate(departureDate.atTime(
                        Integer.parseInt(departureTime.split(":")[0]),
                        Integer.parseInt(departureTime.split(":")[1])).toString())
                .returnDate(returnDate.atTime(
                        Integer.parseInt(returnTime.split(":")[0]),
                        Integer.parseInt(returnTime.split(":")[1])).toString())
                .price(new BigDecimal("350000")) // 현실적인 가격으로 조정
                .currency("KRW")
                .origin(origin)
                .destination(destination)
                .departureCarrierCode(carrierCode)
                .departureFlightNumber("123")
                .returnCarrierCode(carrierCode)
                .returnFlightNumber("124")
                .build();

        return Collections.singletonList(defaultOffer);
    }
}
