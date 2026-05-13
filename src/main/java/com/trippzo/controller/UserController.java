package com.trippzo.controller;

import com.trippzo.exception.PasswordMismatchException;
import com.trippzo.exception.UserAlreadyExistsException;
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
    public String registerUser(@Valid @ModelAttribute("userDto") UserRegisterDTO userDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(userDto);
        } catch (UserAlreadyExistsException e) {
            bindingResult.rejectValue(e.getField(), "error." + e.getField(), e.getMessage());
            return "register";
        } catch (PasswordMismatchException e) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Възникна неочаквана грешка. Моля, опитайте пак.");
            return "register";
        }

        return "redirect:/login?success";
    }

    @GetMapping("/login")
    public String showLoginForm(HttpServletRequest request, Model model, @RequestParam(value = "error", required = false) String error) {
        request.getSession(true);
        if (error != null) {
            model.addAttribute("error", "Невалидно потребителско име или парола.");
        }
        return "login";
    }
}