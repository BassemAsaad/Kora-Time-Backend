package com.app.koratime.auth.dto;

import com.app.koratime.user.dto.UserSummary;
import lombok.Builder;

@Builder
public record AuthResponse (
        String accessToken,
        String refreshToken,
        UserSummary user
) {
}
