package com.volunteerBackend.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.DonateSummaryDTO;
import com.volunteerBackend.model.Donation;

@Component
public class DonationSummaryMapper {
    public DonateSummaryDTO toDTOBasic(Donation donation) {
        DonateSummaryDTO dto = new DonateSummaryDTO();
        if (donation.getIsAnonymous()) {
            dto.setFullName("Nhà hảo tâm ẩn danh");
        } else {
            dto.setFullName(donation.getDonorName());
        }
        dto.setAmount(donation.getAmount());
        dto.setCreatedAt(donation.getCreatedAt());
        return dto;
    }

    public DonateSummaryDTO toDTOByAdmin(Donation donation) {
        DonateSummaryDTO dto = new DonateSummaryDTO();
        dto.setId(donation.getId());
        dto.setFullName(donation.getDonorName());
        dto.setAmount(donation.getAmount());
        dto.setStatus(donation.getPaymentStatus());
        dto.setCreatedAt(donation.getCreatedAt());
        return dto;
    }

    public List<DonateSummaryDTO> toDTOListBasic(List<Donation> donations) {
        return donations.stream()
                .map(this::toDTOBasic)
                .toList();
    }

    public List<DonateSummaryDTO> toDTOListByAdmin(List<Donation> donations) {
        return donations.stream()
                .map(this::toDTOByAdmin)
                .toList();
    }
}
