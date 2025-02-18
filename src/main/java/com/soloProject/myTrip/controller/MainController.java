package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.service.FlightSearchService;
import com.soloProject.myTrip.service.ItemService;
import com.soloProject.myTrip.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final MainService mainService;
    private final ItemService itemService;
    private final FlightSearchService flightSearchService;

    @GetMapping(value = "/")
    public String main(Model model) {
        List<Item> recommendedItems = mainService.getRecommendedItems();
        List<Item> confirmedItems = mainService.getConfirmedItems();

        model.addAttribute("recommendedItems", recommendedItems);
        model.addAttribute("confirmedItems", confirmedItems);

        return "main";
    }
}