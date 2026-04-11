package com.citizenconnect.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citizenconnect.service.AdminDashboardService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class AdminPortalController {

    private final AdminDashboardService adminDashboardService;

    public AdminPortalController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminDashboardService.overview();
    }
}
