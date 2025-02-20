package com.soloProject.myTrip.service;

import com.soloProject.myTrip.constant.AirlineCode;
import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.repository.ItemReservationRepository;
import com.soloProject.myTrip.dto.FlightOfferDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
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
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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

  // 새로운 상품 예약 정보 생성
  @Async
  public CompletableFuture<ItemReservation> createReservationForDateAsync(Item item, LocalDate date) {
    return CompletableFuture.supplyAsync(() -> {
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

        return itemReservationRepository.save(reservation);
      } catch (Exception e) {
        log.error("예약 생성 실패 - 상품: {}, 날짜: {}", item.getId(), date, e);
        return null;
      }
    }, executorService);
  }


  // 특정 날짜의 예약 엔티티 삭제
  @Transactional
  public void deleteReservationForDate(Item item, LocalDate date) {
    String dateTime = date.toString() + "T00:00:00";
    itemReservationRepository.deleteByItemAndDepartureDateTime(item, dateTime);
  }

  // 7일분의 예약 정보 엔티티 생성
  @Transactional
  public void initializeReservations(Item item) {
    LocalDate startDate = LocalDate.now().plusDays(1);
    LocalDate endDate = startDate.plusDays(6);

    List<CompletableFuture<ItemReservation>> futures = new ArrayList<>();

    // 비동기로 모든 날짜의 예약 생성 요청
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      futures.add(createReservationForDateAsync(item, date));
    }

    // 모든 비동기 작업이 완료될 때까지 대기
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenRun(() -> {
          // 모든 예약 생성이 완료된 후 최저가격 업데이트
          updateItemLowestPrice(item);
        })
        .join();
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