package com.volunteerBackend.model;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.volunteerBackend.type.AuthProvider;
import com.volunteerBackend.type.Gender;
import com.volunteerBackend.type.UserRole;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"donations", "providers"})
@ToString(exclude = {"donations", "providers"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = true, unique = true)
    private String email;
    
    @Column(nullable = true)
    private String password;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "AvatarURL", nullable = true)
    private String avatar;

    @Column(name = "PhoneNumber", nullable = true, unique = true)
    private String phoneNumber;
    
    // private String gender;

    @Column(name = "CoverPhotoURL", nullable = true)
    private String coverPhotoURL;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name="gender", nullable = true)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role", nullable = true)
    private UserRole role;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    // === EMAIL VERIFICATION ===
    @Column(name = "verification_token")
    private String verificationToken;
    
    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    // Relationships

    @OneToMany(mappedBy = "donor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donation> donations;

    @OneToMany(
        mappedBy = "user",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private Set<UserProvider> providers = new HashSet<>();
}
