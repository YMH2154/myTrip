package com.soloProject.myTrip.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterController {
    @GetMapping("/footer/agree")
    public String goToAgreePage(){
        return "footer/agree";
    }
    @GetMapping("/footer/legalNotice")
    public String goTolegalPage(){
        return "footer/legalNotice";
    }
}
