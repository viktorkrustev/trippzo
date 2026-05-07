package com.trippzo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LocaleController {

    @GetMapping("/locale")
    public String changeLocale(@RequestParam("lang") String lang, HttpServletRequest request) {

        String referer = request.getHeader("Referer");

        if (referer == null || referer.isEmpty()) {
            return "redirect:/";
        }

        return "redirect:" + referer;
    }
}
