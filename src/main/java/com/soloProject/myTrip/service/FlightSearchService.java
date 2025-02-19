package com.soloProject.myTrip.service;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.soloProject.myTrip.dto.FlightOfferDto;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.exception.FlightSearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class FlightSearchService {
    private final Amadeus amadeus;
    private final RedisTemplate<String, List<FlightOfferDto>> flightOffersRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final long CACHE_DURATION = 60 * 60; // 1시간 캐시

    @Transactional
    public List<FlightOfferDto> searchFlights(String origin, String destination, LocalDate departureDate,
            LocalDate returnDate) {
        String cacheKey = generateCacheKey(origin, destination, departureDate, returnDate);

        // 캐시에서 데이터 조회 - flightOffersRedisTemplate 사용
        List<FlightOfferDto> cachedOffers = flightOffersRedisTemplate.opsForValue().get(cacheKey);

        if (cachedOffers != null && !cachedOffers.isEmpty()) {
            log.info("Cache hit for key: {}", cacheKey);
            return cachedOffers;
        }

        try {
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
                    .toList();

            if (!offers.isEmpty()) {
                // 캐시에 저장 - flightOffersRedisTemplate 사용
                flightOffersRedisTemplate.opsForValue().set(cacheKey, offers, CACHE_DURATION, TimeUnit.SECONDS);
                log.info("Cached flight offers for key: {}", cacheKey);
            }

            return offers;
        } catch (ResponseException e) {
            log.error("Amadeus API 호출 중 오류 발생: {}", e.getMessage());
            throw new FlightSearchException("항공권 검색 중 오류 발생", e);
        }
    }

    @Transactional
    public BigDecimal getLowestFlightPrice(String origin, String destination, LocalDate departureDate,
            LocalDate returnDate) {
        String cacheKey = generateLowestPriceCacheKey(origin, destination, departureDate, returnDate);

        // 캐시에서 최저가 조회 - redisTemplate 사용
        BigDecimal cachedPrice = (BigDecimal) redisTemplate.opsForValue().get(cacheKey);

        if (cachedPrice != null) {
            log.info("Cache hit for lowest price key: {}", cacheKey);
            return cachedPrice;
        }

        try {
            List<FlightOfferDto> offers = searchFlights(origin, destination, departureDate, returnDate);
            BigDecimal lowestPrice = offers.stream()
                    .map(FlightOfferDto::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            // 최저가를 캐시에 저장 - redisTemplate 사용
            redisTemplate.opsForValue().set(cacheKey, lowestPrice, CACHE_DURATION, TimeUnit.SECONDS);
            log.info("Cached lowest price for key: {}", cacheKey);

            return lowestPrice;
        } catch (FlightSearchException e) {
            log.error("최저가 조회 실패", e);
            return BigDecimal.ZERO;
        }
    }

    @Transactional
    private String generateCacheKey(String origin, String destination, LocalDate departureDate, LocalDate returnDate) {
        return String.format("flight:offers:%s:%s:%s:%s", origin, destination, departureDate, returnDate);
    }

    @Transactional
    private String generateLowestPriceCacheKey(String origin, String destination, LocalDate departureDate,
            LocalDate returnDate) {
        return String.format("flight:lowest-price:%s:%s:%s:%s", origin, destination, departureDate, returnDate);
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
}
