package com.lms.lms_backend.service;

import com.lms.lms_backend.dto.request.SignupRequest;
import com.lms.lms_backend.dto.response.AuthUserResponse;
import com.lms.lms_backend.dto.response.PasswordSetupCodeResponse;
import com.lms.lms_backend.enums.UserType;
import com.lms.lms_backend.exception.BadRequestException;
import com.lms.lms_backend.exception.DuplicateResourceException;
import com.lms.lms_backend.exception.ResourceNotFoundException;
import com.lms.lms_backend.model.User;
import com.lms.lms_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${lms.auth.admin.username}") String adminUsername) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
    }

    @Transactional
    public AuthUserResponse signup(SignupRequest request) {
        validatePasswordLength(request.password());
        String email = request.email().trim().toLowerCase();
        if (email.equalsIgnoreCase(adminUsername)) {
            throw new DuplicateResourceException("An account with that email already exists");
        }

        Optional<User> existingAccount = userRepository.findByEmailIgnoreCase(email);
        User user;
        if (existingAccount.isPresent()) {
            user = existingAccount.get();
            if (!user.isActive() || user.getPasswordHash() != null
                    || !isValidSetupCode(user, request.setupCode())) {
                throw new DuplicateResourceException("An account with that email already exists or needs an admin setup code");
            }
            user.setName(request.name().trim());
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            user.setPasswordSetupCodeHash(null);
            user.setPasswordSetupCodeExpiresAt(null);
            userRepository.save(user);
        } else {
            if (request.setupCode() != null && !request.setupCode().isBlank()) {
                throw new BadRequestException("An admin setup code is only valid for a pre-registered student account");
            }
            user = new User();
            user.setName(request.name().trim());
            user.setEmail(email);
            user.setUserType(UserType.STUDENT);
            user.setActive(true);
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            userRepository.save(user);
        }

        return toResponse(user);
    }

    @Transactional
    public PasswordSetupCodeResponse issuePasswordSetupCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive() || user.getPasswordHash() != null) {
            throw new BadRequestException("A setup code can only be issued for an active account without a password");
        }

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        user.setPasswordSetupCodeHash(hashSetupCode(code));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);
        user.setPasswordSetupCodeExpiresAt(expiresAt);
        userRepository.save(user);
        return new PasswordSetupCodeResponse(code, expiresAt);
    }

    public void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("Password cannot exceed 72 UTF-8 bytes");
        }
    }

    public AuthUserResponse getAuthenticatedUser(String username) {
        if (adminUsername.equalsIgnoreCase(username)) {
            return new AuthUserResponse("Administrator", adminUsername, "ADMIN");
        }

        User user = userRepository.findByEmailIgnoreCase(username)
                .filter(User::isActive)
                .orElseThrow(() -> new IllegalStateException("Authenticated account no longer exists"));
        return toResponse(user);
    }

    private AuthUserResponse toResponse(User user) {
        return new AuthUserResponse(user.getName(), user.getEmail(), user.getUserType().name());
    }

    private boolean isValidSetupCode(User user, String suppliedCode) {
        if (suppliedCode == null || suppliedCode.isBlank()
                || user.getPasswordSetupCodeHash() == null
                || user.getPasswordSetupCodeExpiresAt() == null
                || LocalDateTime.now().isAfter(user.getPasswordSetupCodeExpiresAt())) {
            return false;
        }
        return MessageDigest.isEqual(
                user.getPasswordSetupCodeHash().getBytes(StandardCharsets.US_ASCII),
                hashSetupCode(suppliedCode.trim()).getBytes(StandardCharsets.US_ASCII));
    }

    private String hashSetupCode(String code) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
