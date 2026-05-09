package com.trippzo.controller;

import com.trippzo.model.dto.UserRegisterDTO;
import com.trippzo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpServletRequest request, Model model) {
        request.getSession(true);

        model.addAttribute("userDto", new UserRegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userDto") UserRegisterDTO userDto, BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.userDto", "Паролите не съвпадат!");
            return "register";
        }

        if (userService.existsByUsername(userDto.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Това потребителско име вече е заето!");
            return "register";
        }

        if (userService.existsByEmail(userDto.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Този имейл вече е регистриран!");
            return "register";
        }

        try {
            userService.registerUser(userDto);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        return "redirect:/login?success";
    }

    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request, Model model,
            @RequestParam(value = "error", required = false) String error) {
        request.getSession(true);

        if (error != null) {
            model.addAttribute("error", "Невалидно потребителско име или парола.");
        }
        return "login";
    }
}
