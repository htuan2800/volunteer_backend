package com.volunteerBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.volunteerBackend.model.Organizer;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Integer> {
    public Organizer findByName(String name);
    public Boolean existsByName(String name);
}
