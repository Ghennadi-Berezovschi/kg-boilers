package com.kgboilers.controller;

import com.kgboilers.dto.CallbackRequestDto;
import com.kgboilers.service.CallbackRequestService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CallbackController {

    private final CallbackRequestService callbackRequestService;

    public CallbackController(CallbackRequestService callbackRequestService) {
        this.callbackRequestService = callbackRequestService;
    }

    @GetMapping("/callback")
    public String callback(Model model) {
        if (!model.containsAttribute("callbackRequest")) {
            model.addAttribute("callbackRequest", new CallbackRequestDto());
        }
        model.addAttribute("callbackSuccess", false);
        return "callback";
    }

    @PostMapping("/callback")
    public String submitCallback(@Valid @ModelAttribute("callbackRequest") CallbackRequestDto callbackRequest,
                                 BindingResult bindingResult,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("callbackSuccess", false);
            return "callback";
        }

        callbackRequestService.submit(callbackRequest);
        model.addAttribute("callbackSuccess", true);
        model.addAttribute("callbackRequest", new CallbackRequestDto());
        return "callback";
    }
}
