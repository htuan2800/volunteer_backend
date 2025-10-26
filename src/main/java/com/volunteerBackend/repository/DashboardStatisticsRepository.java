package com.volunteerBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.volunteerBackend.model.DashboardStatistics;

@Repository
public interface DashboardStatisticsRepository extends JpaRepository<DashboardStatistics, Long> {
    
}
