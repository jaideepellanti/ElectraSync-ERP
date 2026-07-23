package com.electrasync.controller;

import com.electrasync.service.DashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        boolean isOwner = hasRole(auth, "ROLE_OWNER");
        boolean isManager = hasRole(auth, "ROLE_MANAGER");

        model.addAttribute("username", auth.getName());
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isManager", isManager);

        // Owner and Manager see the full dashboard with statistics.
        // Cashier goes straight to the POS billing screen instead.
        if (isOwner || isManager) {
            model.addAttribute("data", dashboardService.getOwnerDashboardData());
            return "dashboard";
        }
        return "redirect:/pos";
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
