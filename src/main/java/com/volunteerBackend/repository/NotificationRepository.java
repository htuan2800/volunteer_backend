package com.volunteerBackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import com.volunteerBackend.model.Notification;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
}
