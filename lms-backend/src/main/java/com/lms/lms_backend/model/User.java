package com.lms.lms_backend.model;

import com.lms.lms_backend.enums.UserType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    // @Column(unique = true) adds a UNIQUE constraint in the DB.
    // We also check for duplicates in the service layer to return a clean 409.
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Column(unique = true)
    private String email;

    @Column(length = 60)
    private String passwordHash;

    @Column(length = 64)
    private String passwordSetupCodeHash;

    private LocalDateTime passwordSetupCodeExpiresAt;

    // @Enumerated(EnumType.STRING) stores "STUDENT" or "INSTRUCTOR" as text
    // instead of 0/1 integers — much more readable in the database.
    @NotNull(message = "User type is required")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @JsonIgnore
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @JsonIgnore
    public String getPasswordSetupCodeHash() { return passwordSetupCodeHash; }
    public void setPasswordSetupCodeHash(String passwordSetupCodeHash) { this.passwordSetupCodeHash = passwordSetupCodeHash; }

    @JsonIgnore
    public LocalDateTime getPasswordSetupCodeExpiresAt() { return passwordSetupCodeExpiresAt; }
    public void setPasswordSetupCodeExpiresAt(LocalDateTime passwordSetupCodeExpiresAt) {
        this.passwordSetupCodeExpiresAt = passwordSetupCodeExpiresAt;
    }

    public boolean isPasswordSet() { return passwordHash != null; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
