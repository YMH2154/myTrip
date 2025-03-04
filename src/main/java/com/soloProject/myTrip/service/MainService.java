package com.soloProject.myTrip.service;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MainService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<Item> getRecommendedItems() {
        return itemRepository.findRandomItems(8);
    }

    @Transactional(readOnly = true)
    public List<Item> getMostCheapItems() {
        // 조회수와 예약 수를 기준으로 인기 상품 조회
        return itemRepository.findMostCheapItems(PageRequest.of(0, 8));
    }

    @Transactional(readOnly = true)
    public List<Item> getMostReservationCountItems(){
        List<Item> reservationCountItems = itemRepository.findMostReservationCountItems(PageRequest.of(0, 8));
        reservationCountItems.forEach(item -> {
            item.setEarliestDepartureDate(item.getReservations().stream()
                    .map(reservation -> LocalDate.parse(reservation.getDepartureDateTime().split("T")[0]))
                    .min(LocalDate::compareTo)
                    .orElse(null));
            item.setAirline(item.getReservations().stream()
                    .findFirst()
                    .map(reservation -> reservation.getDepartureCarrierName())
                    .orElse(""));
        });
        return reservationCountItems;
    }

}
