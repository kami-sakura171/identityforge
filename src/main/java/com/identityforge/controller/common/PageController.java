package com.identityforge.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer/dashboard";
    }

    @GetMapping("/customer/profile")
    public String customerProfile() {
        return "customer/profile";
    }

    @GetMapping("/customer/avatar")
    public String customerAvatar() {
        return "customer/avatar";
    }

    @GetMapping("/customer/notifications")
    public String customerNotifications() {
        return "customer/notifications";
    }

    @GetMapping("/customer/preferences")
    public String customerPreferences() {
        return "customer/preferences";
    }

    @GetMapping("/customer/verification")
    public String customerVerification() {
        return "customer/verification";
    }

    @GetMapping("/customer/consent")
    public String customerConsent() {
        return "customer/consent";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/customers")
    public String adminCustomerList() {
        return "admin/customer-list";
    }

    @GetMapping("/admin/custom-fields")
    public String adminCustomFields() {
        return "admin/custom-fields";
    }

    @GetMapping("/admin/tos")
    public String adminToS() {
        return "admin/tos-management";
    }

    @GetMapping("/admin/import")
    public String adminImport() {
        return "admin/import";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}
