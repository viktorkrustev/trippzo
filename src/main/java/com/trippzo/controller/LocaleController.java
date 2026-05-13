package com.trippzo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Controller
public class LocaleController {

    @GetMapping("/locale")
    public String changeLocale(@RequestParam("lang") String lang, HttpServletRequest request) {

        String referer = request.getHeader("Referer");

        if (referer == null || referer.isEmpty()) {
            return "redirect:/";
        }

        try {
            URL url = new URL(referer);
            String host = url.getHost();
            String serverName = request.getServerName();

            if (!host.equals(serverName)) {
                log.warn("Опит за Open Redirect през locale промяна! Referer: {}", referer);
                return "redirect:/";
            }

            String path = url.getFile();
            return "redirect:" + path;

        } catch (MalformedURLException e) {
            log.error("Невалиден URL в Referer header-а: {}", referer);
            return "redirect:/";
        }
    }
}