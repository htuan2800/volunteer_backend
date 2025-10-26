package com.volunteerBackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.volunteerBackend.DTO.OrganizerDTO;
import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.mapper.OrganizerMapper;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.request.OrganizerRequest;
import com.volunteerBackend.service.OrganizerService;

@RestController
public class OrganizerController {
    @Autowired
    private OrganizerService organizerService;
    @Autowired
    private OrganizerMapper organizerMapper;

    @GetMapping("/api/organizers")
    public ResponseEntity<List<OrganizerDTO>> getCategories(
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        List<Organizer> organizers = organizerService.getAllOrganizers();
        List<OrganizerDTO> OrganizerDTOs = organizerMapper.toDTOList(organizers);
        return new ResponseEntity<>(OrganizerDTOs, HttpStatus.OK);
    }

    @GetMapping("/api/organizers/{organizerId}")
    public ResponseEntity<OrganizerDTO> getOrganizerById(@PathVariable("organizerId") Integer id) throws UserException {
        Organizer organizer = organizerService.findOrganizerById(id);
        OrganizerDTO OrganizerDTO = organizerMapper.toDTO(organizer);
        return new ResponseEntity<>(OrganizerDTO, HttpStatus.OK);
    }

    @PostMapping("/api/organizers/add_organizer")
    public ResponseEntity<?> createOrganizer(@RequestBody Organizer organizer) throws Exception {
        boolean isSuccess = organizerService.createOrganizer(organizer);
        return new ResponseEntity<>(isSuccess, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/organizers/{organizerId}/delete")
    public ResponseEntity<Map<String, Boolean>> deleteOrganizer(@PathVariable("organizerId") Integer organizerId ) throws UserException {
        boolean isSuccess = organizerService.deleteOrganizer(organizerId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSuccess", isSuccess);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/api/organizers/{organizerId}/active")
    public ResponseEntity<Map<String, Boolean>> setActive(@PathVariable("organizerId") Integer organizerId) throws UserException {
        boolean isSuccess = organizerService.changeActiveOrganizer(organizerId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSuccess", isSuccess);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/api/organizers/update_organizer/{organizerId}")
    public ResponseEntity<?> updateOrganizer(@RequestBody OrganizerRequest organizer, @PathVariable("organizerId") Integer organizerId) throws Exception {
        boolean isSuccess = organizerService.updateOrganizerByAdmin(organizer, organizerId);        
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }
}
