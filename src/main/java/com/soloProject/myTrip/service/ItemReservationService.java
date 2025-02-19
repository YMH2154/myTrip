package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.AirlineCode;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.repository.ItemReservationRepository;
import com.soloProject.myTrip.dto.FlightOfferDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.Comparator;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemReservationService {

  private final ItemReservationRepository itemReservationRepository;
  private final FlightSearchService flightSearchService;

  // 최저가격 업데이트 메서드 추가
  @Transactional
  public void updateItemLowestPrice(Item item) {
    List<ItemReservation> reservations = itemReservationRepository.findByItemId(item.getId());
    if (!reservations.isEmpty()) {
      int lowestPrice = reservations.stream()
          .mapToInt(ItemReservation::getTotalPrice)
          .min()
          .getAsInt();
      item.updateLowestPrice(lowestPrice);
    }
  }

  // 새로운 날짜의 예약 엔티티 생성
  @Transactional
  public void createReservationForDate(Item item, LocalDate date) {
    try {
      // 왕복 항공권 검색
      List<FlightOfferDto> flightOffers = flightSearchService.searchFlights(
          item.getOrigin().name(),
          item.getDestination().name(),
          date,
          date.plusDays(item.getDuration() - 1));

      if (flightOffers.isEmpty()) {
        log.error("항공권 정보를 찾을 수 없음 - 상품: {}, 날짜: {}", item.getId(), date);
        return;
      }

      // 최저가 항공권 선택
      FlightOfferDto offer = flightOffers.get(0); // max=1로 설정했으므로 첫 번째가 최저가
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

      itemReservationRepository.save(reservation);
    } catch (Exception e) {
      log.error("예약 생성 실패 - 상품: {}, 날짜: {}", item.getId(), date, e);
    }
  }

  // 특정 날짜의 예약 엔티티 삭제
  @Transactional
  public void deleteReservationForDate(Item item, LocalDate date) {
    String dateTime = date.toString() + "T00:00:00";
    itemReservationRepository.deleteByItemAndDepartureDateTime(item, dateTime);
  }

  // 다음날부터 7일치 예약 엔티티 생성
  @Transactional
  public void initializeReservations(Item item) {
    LocalDate startDate = LocalDate.now().plusDays(1);
    LocalDate endDate = startDate.plusDays(6);

    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      createReservationForDate(item, date);
    }

    // 모든 예약 생성 후 최저가격 업데이트
    updateItemLowestPrice(item);
  }

  @Transactional
  public ItemReservation getItemReservation(Long itemId, String departureDateTime) {
    return itemReservationRepository.findByItemIdAndDepartureDateTime(itemId, departureDateTime);
  }

  @Transactional
  public List<ItemReservation> getReservationsForItem(Long itemId) {
    return itemReservationRepository.findByItemId(itemId);
  }
}