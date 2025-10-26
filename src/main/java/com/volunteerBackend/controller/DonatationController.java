package com.volunteerBackend.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.DonateSummaryDTO;
import com.volunteerBackend.DTO.DonaterDTO;
import com.volunteerBackend.mapper.DonaterMapper;
import com.volunteerBackend.mapper.DonationSummaryMapper;
import com.volunteerBackend.model.Donation;
import com.volunteerBackend.model.User;
import com.volunteerBackend.request.DonateRequest;
import com.volunteerBackend.service.DonateService;
import com.volunteerBackend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;


@RestController
public class DonatationController {
    @Autowired
    private DonateService donatationService;

    @Autowired
    private UserService userService;

    @Autowired
    private DonationSummaryMapper donationSummaryMapper;

    @Autowired
    private DonaterMapper donaterMapper;

    @PostMapping("/donates/add_donate")
    public ResponseEntity<String> CreateDonate(@RequestBody DonateRequest donateRequest, @RequestHeader(value = "Authorization", required = false) String jwt, HttpServletRequest request) throws UnsupportedEncodingException {
        User user = userService.findUserByJwt(jwt);
        System.out.println(donateRequest.getDonor_amount() + " " + donateRequest.getDonor_email() + " " + donateRequest.getDonor_name() + " " + donateRequest.getDonor_phone() + " " + donateRequest.getMessage());
        String result = donatationService.createDonate(request, donateRequest, user);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/api/donates")
    public ResponseEntity<List<DonateSummaryDTO>> getAllDonates() {
        List<Donation> donates = donatationService.getAllDonates();
        List<DonateSummaryDTO> donatesDTO = donationSummaryMapper.toDTOListByAdmin(donates);
        return new ResponseEntity<>(donatesDTO, HttpStatus.OK);
    }

    @GetMapping("/api/donates/donor")
    public ResponseEntity<List<DonateSummaryDTO>> getAllDonatesByDonorName(@RequestParam(required = false) String name) {
        List<Donation> donates = donatationService.getAllDonatesByDonorName(name);
        List<DonateSummaryDTO> donatesDTO = donationSummaryMapper.toDTOListByAdmin(donates);
        return new ResponseEntity<>(donatesDTO, HttpStatus.OK);
    }

    @GetMapping("/donates/campaign/{id}")
    public ResponseEntity<List<DonateSummaryDTO>> getAllDonatesByCampaign(@PathVariable Long id) {
        List<Donation> donates = donatationService.getAllDonatesByCampaign(id);
        List<DonateSummaryDTO> donatesDTO = donationSummaryMapper.toDTOListBasic(donates);
        return new ResponseEntity<>(donatesDTO, HttpStatus.OK);
    }

    @GetMapping("/api/donates/{donateId}")
    public ResponseEntity<DonaterDTO> getDonate(@PathVariable Long donateId) {
        Donation donate = donatationService.getDonationById(donateId);
        DonaterDTO donateDTO = donaterMapper.toDTO(donate);
        return new ResponseEntity<>(donateDTO, HttpStatus.OK);
    }

}
