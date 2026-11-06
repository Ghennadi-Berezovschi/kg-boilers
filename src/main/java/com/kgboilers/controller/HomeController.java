package com.kgboilers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/."})
    public String home() {
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("aboutPage", true);
        return "about";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }
}
