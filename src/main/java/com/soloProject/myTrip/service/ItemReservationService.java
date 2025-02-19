package com.soloProject.myTrip.service;

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
  public void createReservationForDate(Item item, LocalDate date) {
    try {
      // 항공권 정보 조회
      List<FlightOfferDto> flightOffers = flightSearchService.searchFlights(
          item.getOrigin().name(),
          item.getDestination().name(),
          date,
          date.plusDays(item.getDuration() - 1));

      // 최저가 항공권 선택
      FlightOfferDto lowestPriceOffer = flightOffers.stream()
          .min(Comparator.comparing(FlightOfferDto::getPrice))
          .orElseThrow(() -> new RuntimeException("항공권 정보를 찾을 수 없습니다."));

      int flightPrice = lowestPriceOffer.getPrice().intValue();
      int totalPrice = item.getPrice() + flightPrice;

      ItemReservation reservation = ItemReservation.builder()
          .item(item)
          .reservationDate(date)
          .totalPrice(totalPrice)
          .carrierCode(lowestPriceOffer.getCarrierCode())
          .carrierName(lowestPriceOffer.getCarrierName())
          .flightPrice(flightPrice)
          .build();

      itemReservationRepository.save(reservation);
    } catch (Exception e) {
      log.error("예약 생성 실패 - 상품: {}, 날짜: {}", item.getId(), date, e);
      // 항공권 정보 조회 실패 시 상품 가격만으로 예약 생성
      ItemReservation reservation = ItemReservation.builder()
          .item(item)
          .reservationDate(date)
          .totalPrice(item.getPrice())
          .build();

      itemReservationRepository.save(reservation);
    }
  }

  // 특정 날짜의 예약 엔티티 삭제
  public void deleteReservationForDate(Item item, LocalDate date) {
    itemReservationRepository.deleteByItemAndReservationDate(item, date);
  }

  // 다음날부터 7일치 예약 엔티티 생성
  public void initializeReservations(Item item) {
    LocalDate startDate = LocalDate.now().plusDays(1);
    LocalDate endDate = startDate.plusDays(6);

    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      createReservationForDate(item, date);
    }

    // 모든 예약 생성 후 최저가격 업데이트
    updateItemLowestPrice(item);
  }

  public ItemReservation getItemReservation(Long itemId, LocalDate reservationDate) {
    return itemReservationRepository.findByItemIdAndReservationDate(itemId, reservationDate);
  }

  public List<ItemReservation> getReservationsForItem(Long itemId) {
    return itemReservationRepository.findByItemId(itemId);
  }
}