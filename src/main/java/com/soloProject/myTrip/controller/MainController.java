package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.service.ItemService;
import com.soloProject.myTrip.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping(value = "/")
    public String main(Model model) {
        List<Item> deadlineItems = mainService.getDeadlineItems();
        List<Item> confirmedItems = mainService.getConfirmedItems();

        model.addAttribute("deadlineItems", deadlineItems);
        model.addAttribute("confirmedItems", confirmedItems);

        return "main";
    }
}
