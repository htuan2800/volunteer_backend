package com.volunteerBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.volunteerBackend.DTO.OrganizerStatsDTO;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.type.PaymentStatus;

@Repository
public interface OrganizerRepository extends JpaRepository<Organizer, Integer> {
    public Organizer findByName(String name);
    public Boolean existsByName(String name);

    @Query("SELECT new com.volunteerBackend.DTO.OrganizerStatsDTO(" +
           "  COUNT(DISTINCT c.id), " +
           "  COALESCE(COUNT(DISTINCT d.id), 0L), " +
           "  COALESCE(SUM(d.amount), 0.0)) " +
           "FROM Organizer o " +
           "LEFT JOIN o.campaigns c " +
           "LEFT JOIN c.donations d ON d.paymentStatus = :successStatus " +
           "WHERE o.organizerId = :organizerId")
    OrganizerStatsDTO getOrganizerStats(
        @Param("organizerId") Integer organizerId, 
        @Param("successStatus") PaymentStatus successStatus
    );
}
