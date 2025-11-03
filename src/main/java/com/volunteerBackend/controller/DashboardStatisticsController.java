package com.volunteerBackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.DashboardStatisticsDTO;
import com.volunteerBackend.model.DashboardStatistics;
import com.volunteerBackend.service.DashboardStatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DashboardStatisticsController {

    private final DashboardStatisticsService dashboardStatisticsService;

    @GetMapping("/dashboardStatistics")
    public ResponseEntity<DashboardStatistics> getStaticticsUserPanel() throws Exception {
        DashboardStatistics datas=dashboardStatisticsService.getDashboardStatistics();
        return new ResponseEntity<>(datas, HttpStatus.OK);
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<DashboardStatisticsDTO> getStatistics(
            @RequestParam(defaultValue = "7") int days) {
        
        if (days <= 0 || days > 365) {
            return ResponseEntity.badRequest().build();
        }
        
        DashboardStatisticsDTO response = dashboardStatisticsService.getStatistics(days);
        return ResponseEntity.ok(response);
    }
}
