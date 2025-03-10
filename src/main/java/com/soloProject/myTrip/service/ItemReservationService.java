package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.AirlineCode;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.repository.ItemRepository;
import com.soloProject.myTrip.repository.ItemReservationRepository;
import com.soloProject.myTrip.dto.FlightOfferDto;
import com.soloProject.myTrip.dto.ItemReservationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemReservationService {

  private final ItemReservationRepository itemReservationRepository;
  private final FlightSearchService flightSearchService;
  private final ItemRepository itemRepository;

  // 새로운 상품 예약 정보 생성
  @Async
  public void createReservationForDateAsync(Item item, LocalDate date) {
    CompletableFuture.supplyAsync(() -> {
      try {
        return createReservationForDate(item, date);
      } catch (Exception e) {
        log.error("예약 생성 실패 - 상품: {}, 날짜: {}", item, date, e);
        return null;
      }
    });
  }

  // 7일분의 예약 정보 엔티티 생성
  @Transactional
  public void initializeReservations(Item item) {
    LocalDate startDate = LocalDate.now().plusDays(1);
    LocalDate endDate = startDate.plusDays(6);

    // 동기적으로 순차 처리
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      try {
        ItemReservation reservation = createReservationForDate(item, date);
        if (reservation != null) {
          log.info("예약 생성 성공 - 상품: {}, 날짜: {}", item.getId(), date);
        }
      } catch (Exception e) {
        log.error("예약 생성 실패 - 상품: {}, 날짜: {}", item.getId(), date, e);
        // 한 날짜 실패해도 계속 진행
      }
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  protected ItemReservation createReservationForDate(Item item, LocalDate date) {
    try {
      List<FlightOfferDto> flightOffers = flightSearchService.searchFlights(
          item.getOrigin().name(),
          item.getDestination().name(),
          date,
          date.plusDays(item.getDuration() - 1));

      if (flightOffers.isEmpty()) {
        log.error("항공권 정보를 찾을 수 없음 - 상품: {}, 날짜: {}", item.getId(), date);
        return null;
      }

      FlightOfferDto offer = flightOffers.get(0);
      int totalPrice = item.getPrice() + offer.getPrice().intValue();

      ItemReservation reservation = ItemReservation.builder()
          .item(item)
          .departureDateTime(offer.getDepartureDate())
          .returnDateTime(offer.getReturnDate())
          .totalPrice(totalPrice)
          .departureCarrierCode(offer.getDepartureCarrierCode())
          .departureCarrierName(AirlineCode.getCompanyNameByCode(offer.getDepartureCarrierCode()))
          .departureFlightNumber(offer.getDepartureFlightNumber())
          .returnCarrierCode(offer.getReturnCarrierCode())
          .returnCarrierName(AirlineCode.getCompanyNameByCode(offer.getReturnCarrierCode()))
          .returnFlightNumber(offer.getReturnFlightNumber())
          .build();

      return itemReservationRepository.saveAndFlush(reservation);
    } catch (Exception e) {
      log.error("예약 생성 중 오류 발생 - 상품: {}, 날짜: {}", item.getId(), date, e);
      throw e;
    }
  }

  // 특정 날짜의 예약 엔티티 삭제
  @Transactional
  public void deleteReservationForDate(Item item, LocalDate date) {
    String dateTime = date.toString() + "T00:00:00";
    itemReservationRepository.deleteByItemAndDepartureDateTime(item, dateTime);
  }

  @Transactional
  public ItemReservation getItemReservation(Long itemId, String departureDateTime) {
    return itemReservationRepository.findByItemIdAndDepartureDateTime(itemId, departureDateTime);
  }

  @Transactional
  public List<ItemReservation> getReservationsForItem(Long itemId) {
    return itemReservationRepository.findByItemId(itemId);
  }

  public ItemReservationDto getItemReservationInfo(Long itemId) {
    List<ItemReservation> reservations = getReservationsForItem(itemId);
    ItemReservationDto dto = ItemReservationDto.createEmpty();

    int minPrice = Integer.MAX_VALUE;
    int maxPrice = Integer.MIN_VALUE;

    // 내일부터 7일 동안의 날짜 범위 설정
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    LocalDate endDate = tomorrow.plusDays(6);

    for (ItemReservation reservation : reservations) {
      String departureDate = reservation.getDepartureDateTime().split("T")[0];
      LocalDate reservationDate = LocalDate.parse(departureDate);

      // 날짜가 범위 내에 있는 경우에만 처리
      if (!reservationDate.isBefore(tomorrow) && !reservationDate.isAfter(endDate)) {
        // 잔여석, 가격, 상태 정보 설정
        dto.getRemainingSeats().put(departureDate, reservation.getRemainingSeats());
        dto.getPrices().put(departureDate, reservation.getTotalPrice());
        dto.getItemSellStatus().put(departureDate, reservation.getItemSellStatus());

        // 최소/최대 가격 업데이트
        int totalPrice = reservation.getTotalPrice();
        if (totalPrice < minPrice)
          minPrice = totalPrice;
        if (totalPrice > maxPrice)
          maxPrice = totalPrice;

        // 출발 항공편 정보
        ItemReservationDto.FlightInfo departureFlight = ItemReservationDto.FlightInfo.builder()
            .carrierName(reservation.getDepartureCarrierName())
            .flightNumber(reservation.getDepartureFlightNumber())
            .dateTime(reservation.getDepartureDateTime())
            .build();

        // 귀국 항공편 정보
        ItemReservationDto.FlightInfo returnFlight = ItemReservationDto.FlightInfo.builder()
            .carrierName(reservation.getReturnCarrierName())
            .flightNumber(reservation.getReturnFlightNumber())
            .dateTime(reservation.getReturnDateTime())
            .build();

        // 출발 확정 정보
        dto.getDepartureConfirmed().put(departureDate, reservation.getDepartureConfirmed());

        // 출발 확정된 날짜의 departureDateTime 설정
        if (reservation.getDepartureConfirmed()) {
          dto.setDepartureDateTime(reservation.getDepartureDateTime());
          log.info("출발 확정 날짜 : {}", departureDate);
        }

        dto.getDepartureFlights().put(departureDate, departureFlight);
        dto.getReturnFlights().put(departureDate, returnFlight);
      }
    }

    dto.setMinPrice(minPrice != Integer.MAX_VALUE ? minPrice : 0);
    dto.setMaxPrice(maxPrice != Integer.MIN_VALUE ? maxPrice : 0);
    return dto;
  }
}