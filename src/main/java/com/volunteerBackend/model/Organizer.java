package com.volunteerBackend.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "organizers")
@EqualsAndHashCode (exclude = {"campaigns"})
@ToString (exclude = {"campaigns"})
public class Organizer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer organizerId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "logo_url", nullable = true)
    private String logoUrl;

    @Column(name="slug")
    private String slug;

    @Column(name="hotline")
    private String hotline;

    @Column(name="email")
    private String email;

    @Column(name="active")
    private Boolean isActive = false;

    @Column(name="is_deleted")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Campaign> campaigns;
}