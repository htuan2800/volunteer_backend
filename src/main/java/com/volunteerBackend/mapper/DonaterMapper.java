package com.volunteerBackend.mapper;

import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.DonaterDTO;
import com.volunteerBackend.model.Donation;

@Component
public class DonaterMapper {
    public DonaterDTO toDTO(Donation donation) {
        DonaterDTO dto = new DonaterDTO();
        dto.setId(donation.getId());
        dto.setDonorName(donation.getDonorName());
        dto.setDonorEmail(donation.getDonorEmail());
        dto.setDonorPhone(donation.getDonorPhone());
        dto.setAmount(donation.getAmount());
        dto.setMessage(donation.getMessage());
        dto.setIsAnonymous(donation.getIsAnonymous());
        dto.setPaymentMethod(donation.getPaymentMethod());
        dto.setVnpTransactionNo(donation.getVnpTransactionNo());
        dto.setPaymentStatus(donation.getPaymentStatus());
        dto.setCreatedAt(donation.getCreatedAt());
        dto.setPaymentDate(donation.getPaymentDate());
        return dto;
    }
}
