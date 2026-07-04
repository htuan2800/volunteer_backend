package com.volunteerBackend.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.CampaignSummaryDTO;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.volunteerBackend.DTO.CampaignImageDTO;
import com.volunteerBackend.model.Campaign;
import com.volunteerBackend.model.Donation;
import com.volunteerBackend.type.PaymentStatus;

@Component
public class CampaignSummaryMapper {

    private final OrganizerMapper organizerMapper;

    private final Cloudinary cloudinary;

    public CampaignSummaryMapper(OrganizerMapper organizerMapper,
            Cloudinary cloudinary) {
        this.organizerMapper = organizerMapper;
        this.cloudinary = cloudinary;
    }

    public CampaignSummaryDTO toDTOBasic(Campaign campaign) {
        CampaignSummaryDTO dto = new CampaignSummaryDTO();
        dto.setCampaignId(campaign.getId());
        dto.setTitle(campaign.getTitle());
        dto.setTargetAmount(campaign.getTargetAmount());
        dto.setCurrentAmount(caculateCurrentAmount(campaign));
        var transformation = new Transformation<>()
                .width(800)
                .crop("scale")
                .quality("auto")
                .fetchFormat("auto");
        String eagerUrl = cloudinary.url()
                .transformation(transformation)
                .generate(campaign.getFeaturedImage());
        dto.setFeaturedImage(eagerUrl);
        // dto.setFeaturedImage(fileStorageProperties.getBaseUrl() +
        // campaign.getFeaturedImage());
        dto.setStoryInfo(campaign.getStoryInfo());
        dto.setSupportCount(campaign.getDonations().size());
        dto.setOrganizer(organizerMapper.toDTO(campaign.getOrganizer()));
        dto.setStatus(campaign.getStatus());
        // Period period = Period.between(LocalDate.now(), campaign.getEndDate());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = campaign.getEndDate();
        long totalDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        dto.setDayLeft(totalDaysBetween);
        dto.setPercentage(calculatePercentage(campaign));
        return dto;
    }

    public Integer calculatePercentage(Campaign campaign) {
        List<Donation> donations = campaign.getDonations();

        // Tính tổng tiền donate
        BigDecimal totalAmount = donations.stream()
                .filter(d -> d.getPaymentStatus() == PaymentStatus.COMPLETED)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính phần trăm = totalAmount * 100 / targetAmount
        BigDecimal percentage = totalAmount
                .multiply(BigDecimal.valueOf(100)) // nhân 100
                .divide(campaign.getTargetAmount(), 0, RoundingMode.DOWN); // chia và làm tròn

        return percentage.intValue(); // convert BigDecimal -> Integer
    }

    public BigDecimal caculateCurrentAmount(Campaign campaign) {
        List<Donation> donations = campaign.getDonations();

        // Tính tổng tiền donate
        BigDecimal totalAmount = donations.stream()
                .filter(d -> d.getPaymentStatus() == PaymentStatus.COMPLETED)
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính phần trăm = totalAmount * 100 / targetAmount
        // BigDecimal currentAmount = totalAmount
        // .multiply(BigDecimal.valueOf(100)); // nhân 100
        return totalAmount;// convert BigDecimal -> Integer
    }

    public CampaignSummaryDTO toDTOBasicNotStoryInfo(Campaign campaign) {
        CampaignSummaryDTO dto = new CampaignSummaryDTO();
        dto.setCampaignId(campaign.getId());
        dto.setTitle(campaign.getTitle());
        dto.setTargetAmount(campaign.getTargetAmount());
        var transformation = new Transformation<>()
                .width(800)
                .crop("scale")
                .quality("auto")
                .fetchFormat("auto");
        String eagerUrl = cloudinary.url()
                .transformation(transformation)
                .generate(campaign.getFeaturedImage());
        dto.setFeaturedImage(eagerUrl);
        // dto.setFeaturedImage(fileStorageProperties.getBaseUrl() +
        // campaign.getFeaturedImage());
        dto.setSupportCount(campaign.getDonations().size());
        dto.setPercentage(calculatePercentage(campaign));
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = campaign.getEndDate();
        long totalDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        dto.setDayLeft(totalDaysBetween);
        return dto;
    }

    public CampaignSummaryDTO toDTOWithImage(Campaign campaign) {
        CampaignSummaryDTO dto = toDTOBasic(campaign);
        var transformation = new Transformation<>()
                .width(800)
                .crop("scale")
                .quality("auto")
                .fetchFormat("auto");
        // Map List<CampaignImage> sang List<CampaignImageDTO>
        if (campaign.getImages() != null) {
            List<CampaignImageDTO> imageDTOs = campaign.getImages().stream()
                    .map(campaignImage -> new CampaignImageDTO(
                            campaignImage.getId(),
                            campaignImage.getImageUrl(),
                            cloudinary.url()
                                    .transformation(transformation)
                                    .generate(campaignImage.getImageUrl()),
                            campaignImage.getSortOrder()))
                    .toList();
            dto.setCampaignImages(imageDTOs);
        } else {
            dto.setCampaignImages(Collections.emptyList());
        }

        return dto;
    }

    // --- List DTO ---
    public List<CampaignSummaryDTO> toDTOListBasic(List<Campaign> campaigns) {
        return campaigns.stream()
                .map(this::toDTOBasic)
                .toList();
    }

    public List<CampaignSummaryDTO> toDTOListBasicNotStoryInfo(List<Campaign> campaigns) {
        return campaigns.stream()
                .map(this::toDTOBasicNotStoryInfo)
                .toList();
    }

    public List<CampaignSummaryDTO> toDTOListWithImage(List<Campaign> campaigns) {
        return campaigns.stream()
                .map(this::toDTOWithImage)
                .toList();
    }
}
