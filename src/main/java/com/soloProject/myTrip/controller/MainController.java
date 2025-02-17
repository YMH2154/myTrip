package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.service.FlightSearchService;
import com.soloProject.myTrip.service.ItemService;
import com.soloProject.myTrip.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final MainService mainService;
    private final FlightSearchService flightSearchService;

    @GetMapping(value = "/")
    public String main(Model model) {
        List<Item> deadlineItems = mainService.getDeadlineItems();
        List<Item> confirmedItems = mainService.getConfirmedItems();

        // 각 상품에 대해 향후 30일간의 가격 정보 계산
        Map<Long, Map<String, Integer>> itemPrices = new HashMap<>();

        Stream.concat(deadlineItems.stream(), confirmedItems.stream())
                .forEach(item -> {
                    Map<String, Integer> datePrices = calculateFuturePrices(item);
                    itemPrices.put(item.getId(), datePrices);

                    // 내일 날짜의 총 가격을 기본 표시 가격으로 설정
                    LocalDate tomorrow = LocalDate.now().plusDays(1);
                    int defaultTotalPrice = datePrices.get(tomorrow.toString());
                    item.setTotalPrice(defaultTotalPrice);
                });

        model.addAttribute("deadlineItems", deadlineItems);
        model.addAttribute("confirmedItems", confirmedItems);
        model.addAttribute("itemPrices", itemPrices);

        return "main";
    }

    private Map<String, Integer> calculateFuturePrices(Item item) {
        Map<String, Integer> datePrices = new HashMap<>();
        LocalDate startDate = LocalDate.now().plusDays(1); // 내일부터
        LocalDate endDate = startDate.plusDays(30); // 30일간

        for (LocalDate departureDate = startDate; !departureDate.isAfter(endDate); departureDate = departureDate
                .plusDays(1)) {
            try {
                // 출발일로부터 item.getDuration()-1일 후를 귀국일로 설정
                LocalDate returnDate = departureDate.plusDays(item.getDuration());

                BigDecimal flightPrice = flightSearchService.getLowestFlightPrice(
                        item.getOrigin().getCode(),
                        item.getDestination().getCode(),
                        departureDate,
                        returnDate);
                int totalPrice = item.getPrice() + flightPrice.intValue();
                datePrices.put(departureDate.toString(), totalPrice);
            } catch (Exception e) {
                log.error("가격 계산 실패 - 상품: {}, 날짜: {}", item.getId(), departureDate, e);
                datePrices.put(departureDate.toString(), item.getPrice()); // 에러 시 기본 가격만 설정
            }
        }
        return datePrices;
    }
}