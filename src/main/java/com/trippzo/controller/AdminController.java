package com.trippzo.controller;

import com.trippzo.service.AdminService;
import com.trippzo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController extends BaseController {

    private final AdminService adminService;

    public AdminController(UserService userService, AdminService adminService) {
        super(userService);
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        AdminService.AdminStats stats = adminService.getAdminStats();
        model.addAttribute("stats", stats);
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin-users";
    }

    @GetMapping("/trips")
    public String listTrips(Model model) {
        model.addAttribute("trips", adminService.getAllTrips());
        return "admin-trips";
    }

    @PostMapping("/users/{userId}/role/promote")
    public String promoteUserToAdmin(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            adminService.promoteUserToAdmin(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User promoted to admin successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to promote user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/role/demote")
    public String demoteUserToNormal(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            adminService.demoteUserToNormal(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User demoted to normal successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to demote user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/trips/{tripId}/delete")
    public String deleteTrip(@PathVariable Long tripId, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteTrip(tripId);
            redirectAttributes.addFlashAttribute("successMessage", "Trip deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete trip: " + e.getMessage());
        }
        return "redirect:/admin/trips";
    }
}
