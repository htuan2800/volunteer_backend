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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.volunteerBackend.DTO.OrganizerDTO;
import com.volunteerBackend.DTO.OrganizerStatsDTO;
import com.volunteerBackend.exceptions.UserException;
import com.volunteerBackend.mapper.OrganizerMapper;
import com.volunteerBackend.model.Organizer;
import com.volunteerBackend.request.OrganizerRequest;
import com.volunteerBackend.service.OrganizerService;

@RestController
@RequestMapping("/api")
public class OrganizerController {
    @Autowired
    private OrganizerService organizerService;
    @Autowired
    private OrganizerMapper organizerMapper;

    @GetMapping("/organizers/{organizerId}")
    public ResponseEntity<OrganizerDTO> getOrganizerById(@PathVariable Integer organizerId) throws UserException {
        Organizer organizer = organizerService.findOrganizerById(organizerId);
        OrganizerDTO OrganizerDTO = organizerMapper.toDTO(organizer);
        return new ResponseEntity<>(OrganizerDTO, HttpStatus.OK);
    }

    @GetMapping("/organizers/dashboard/{organizerId}")
    public ResponseEntity<OrganizerStatsDTO> getgetDashboardDataByOrganizerById(@PathVariable Integer organizerId) throws UserException {
        OrganizerStatsDTO organizer = organizerService.findDashboardDataByOrganizerById(organizerId);
        return new ResponseEntity<>(organizer, HttpStatus.OK);
    }

    @GetMapping("/admin/organizers")
    public ResponseEntity<List<OrganizerDTO>> getAllOrganizers(
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        List<Organizer> organizers = organizerService.getAllOrganizers();
        List<OrganizerDTO> OrganizerDTOs = organizerMapper.toDTOList(organizers);
        return new ResponseEntity<>(OrganizerDTOs, HttpStatus.OK);
    }

    @PostMapping("/admin/organizers/add_organizer")
    public ResponseEntity<?> createOrganizer(@RequestBody Organizer organizer) throws Exception {
        boolean isSuccess = organizerService.createOrganizer(organizer);
        return new ResponseEntity<>(isSuccess, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/organizers/{organizerId}/delete")
    public ResponseEntity<Map<String, Boolean>> deleteOrganizer(@PathVariable Integer organizerId ) throws UserException {
        boolean isSuccess = organizerService.deleteOrganizer(organizerId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSuccess", isSuccess);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/organizers/{organizerId}/active")
    public ResponseEntity<Map<String, Boolean>> setActive(@PathVariable Integer organizerId) throws UserException {
        boolean isSuccess = organizerService.changeActiveOrganizer(organizerId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isSuccess", isSuccess);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/organizers/update_organizer/{organizerId}")
    public ResponseEntity<?> updateOrganizer(@RequestBody OrganizerRequest organizer, @PathVariable Integer organizerId) throws Exception {
        boolean isSuccess = organizerService.updateOrganizerByAdmin(organizer, organizerId);        
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }
}
