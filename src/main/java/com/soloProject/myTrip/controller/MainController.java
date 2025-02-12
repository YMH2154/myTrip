package com.soloProject.myTrip.controller;

import com.soloProject.myTrip.entity.Item;
import com.soloProject.myTrip.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping(value = "/")
    public String main(Model model) {
        List<Item> deadlineItems = itemService.getDeadlineItems();
        List<Item> confirmedItems = itemService.getConfirmedItems();

        model.addAttribute("deadlineItems", deadlineItems);
        model.addAttribute("confirmedItems", confirmedItems);

        return "main";
    }
}
