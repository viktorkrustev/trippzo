package com.trippzo.controller;

import com.trippzo.exception.PasswordMismatchException;
import com.trippzo.exception.UserAlreadyExistsException;
import com.trippzo.model.User;
import com.trippzo.model.dto.PasswordResetDTO;
import com.trippzo.model.dto.UserRegisterDTO;
import com.trippzo.service.EmailService;
import com.trippzo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

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
    public String showLoginForm(HttpServletRequest request, Model model,
            @RequestParam(value = "error", required = false) String error) {
        request.getSession(true);
        if (error != null) {
            model.addAttribute("error", "Невалидно потребителско име или парола.");
        }
        return "login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model, HttpServletRequest request) {
        try {
            String token = userService.initiatePasswordReset(email);

            String resetLink = buildResetLink(request, token);

            emailService.sendPasswordResetEmail(email, resetLink);

        } catch (Exception e) {
            System.out.println("Грешка при reset: " + e.getMessage());
        }

        model.addAttribute("message", "Ако акаунтът с този имейл съществува, ще получите имейл с инструкции.");
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        User user = userService.getUserByResetToken(token);
        if (!userService.isResetTokenValid(user)) {
            model.addAttribute("error", "Невалиден или изтекъл reset линк.");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        model.addAttribute("passwordResetDto", new PasswordResetDTO());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
            @Valid @ModelAttribute("passwordResetDto") PasswordResetDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", token);
            return "reset-password";
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Паролите не съвпадат.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        User user = userService.getUserByResetToken(token);
        if (!userService.isResetTokenValid(user)) {
            model.addAttribute("error", "Невалиден или изтекъл reset линк.");
            return "redirect:/login";
        }

        userService.resetPassword(user, dto.getPassword());
        return "redirect:/login?success";
    }

    private String buildResetLink(HttpServletRequest request, String token) {
        int port = request.getServerPort();
        String portPart = (port != 80 && port != 443) ? ":" + port : "";
        return request.getScheme() + "://" + request.getServerName() + portPart + "/reset-password?token=" + token;
    }
}
