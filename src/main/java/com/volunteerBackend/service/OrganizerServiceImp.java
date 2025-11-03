package com.volunteerBackend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.volunteerBackend.DTO.OrganizerStatsDTO;
import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.repository.OrganizerRepository;
import com.volunteerBackend.request.OrganizerRequest;

@Service
public class OrganizerServiceImp implements OrganizerService {
    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private DashboardStatisticsService dashboardStatisticsService;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public List<Organizer> getAllOrganizers() {
        return organizerRepository.findAll();
    }

    @Override
    public Organizer findOrganizerById(Integer id) {
        return organizerRepository.findById(id).orElse(null);
    }

    @Override
    public OrganizerStatsDTO findDashboardDataByOrganizerById(Integer id) {
        return organizerRepository.getOrganizerStats(id, com.volunteerBackend.type.PaymentStatus.COMPLETED);
    }

    @Override
    public boolean createOrganizer(Organizer organizer) throws Exception {
        if (organizerRepository.existsByName(organizer.getName())) {
            throw new Exception("Organizer already exists");
        }
        organizerRepository.save(organizer);
        dashboardStatisticsService.updateTotalOrganizers();
        return true;
    }

    @Override
    public boolean deleteOrganizer(Integer organizerId) throws UserException {
        Organizer organizer = organizerRepository.findById(organizerId).orElse(null);
        if (organizer == null) {
            throw new UserException("Organizer not found");
        }
        organizer.setIsDeleted(true);
        organizerRepository.save(organizer);
        return true;
    }

    @Override
    public boolean changeActiveOrganizer(Integer organizerId) throws UserException {
        Organizer organizer = organizerRepository.findById(organizerId).orElse(null);
        if (organizer == null) {
            throw new UserException("Organizer not found");
        }
        organizer.setIsActive(!organizer.getIsActive());
        organizerRepository.save(organizer);
        return true;
    }

    @Override
    public boolean updateOrganizerByAdmin(OrganizerRequest organizer, Integer organizerId) throws Exception {
        Organizer existingOrganizer = organizerRepository.findById(organizerId).orElse(null);
        if (existingOrganizer == null) {
            throw new Exception("Organizer not found");
        }
        existingOrganizer.setName(organizer.getName());
        existingOrganizer.setDescription(organizer.getDescription());
        existingOrganizer.setSlug(organizer.getSlug());
        if(organizer.getOption().equals("IMAGE"))
        {
            fileStorageService.deleteFile(existingOrganizer.getLogoUrl());
            existingOrganizer.setLogoUrl(organizer.getLogoUrl());
        }
        existingOrganizer.setHotline(organizer.getHotline());
        existingOrganizer.setEmail(organizer.getEmail());
        organizerRepository.save(existingOrganizer);
        return true;
    }
}
