package com.volunteerBackend.service;

import java.util.List;

import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.request.OrganizerRequest;

public interface OrganizerService {
    public boolean createOrganizer(Organizer organizer) throws Exception;
    public List<Organizer> getAllOrganizers();
    public Organizer findOrganizerById(Integer id);
    public boolean deleteOrganizer(Integer organizerId) throws UserException;
    public boolean changeActiveOrganizer(Integer organizerId) throws UserException; 
    public boolean updateOrganizerByAdmin(OrganizerRequest organizer, Integer organizerId) throws Exception;
}
