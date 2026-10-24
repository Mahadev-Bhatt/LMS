package com.lms.lms_backend.dto.response;

import java.time.LocalDateTime;

public record PasswordSetupCodeResponse(String code, LocalDateTime expiresAt) {}
