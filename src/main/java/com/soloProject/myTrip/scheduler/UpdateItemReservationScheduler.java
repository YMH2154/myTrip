package com.soloProject.myTrip.scheduler;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.entity.ItemReservation;
import com.soloProject.myTrip.repository.ItemReservationRepository;
import com.soloProject.myTrip.service.FlightSearchService;
import com.soloProject.myTrip.service.ItemReservationService;
import com.soloProject.myTrip.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class UpdateItemReservationScheduler {

    private final ItemService itemService;
    private final ItemReservationService itemReservationService;

    // 날짜 변경에 따른 예약 엔티티 업데이트
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional
    public void updateReservations() {
        List<Item> items = itemService.findAllItems();
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(15);

        for (Item item : items) {
            // 오늘 날짜의 예약 엔티티 삭제
            itemReservationService.deleteReservationForDate(item, today);

            // 15일 후 날짜의 예약 엔티티 생성
            itemReservationService.createReservationForDate(item, futureDate);

            // 예약 변경 후 최저가격 업데이트
            itemReservationService.updateItemLowestPrice(item);
        }
    }
}
