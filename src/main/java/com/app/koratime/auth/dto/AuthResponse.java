package com.app.koratime.auth.dto;

import com.app.koratime.user.dto.UserSummary;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AuthResponse {
    @Builder.Default
    String type = "Bearer";
    String accessToken;
    String refreshToken;
    UserSummary user;
}
