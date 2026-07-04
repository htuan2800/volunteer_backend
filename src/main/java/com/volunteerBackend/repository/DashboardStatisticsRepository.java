package com.volunteerBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.volunteerBackend.model.DashboardStatistics;


public interface DashboardStatisticsRepository extends JpaRepository<DashboardStatistics, Long> {
    
}
