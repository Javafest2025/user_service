package org.solace.scholar_ai.user_service.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "avatar_key")
    private String avatarKey; // stores Cloudinary public_id

    @Column(name = "avatar_etag")
    private String avatarEtag; // unused with Cloudinary, retained for compatibility

    @Column(name = "avatar_updated_at")
    private Instant avatarUpdatedAt;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private Instant dateOfBirth;

    @Column(name = "bio")
    private String bio;

    @Column(name = "affiliation")
    private String affiliation;

    @Column(name = "position_title")
    private String positionTitle;

    @Column(name = "research_interests")
    private String researchInterests;

    @Column(name = "google_scholar_url")
    private String googleScholarUrl;

    @Column(name = "personal_website_url")
    private String personalWebsiteUrl;

    @Column(name = "orcid_id")
    private String orcidId;

    @Column(name = "linkedin_url")
    private String linkedInUrl;

    @Column(name = "twitter_url")
    private String twitterUrl;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
