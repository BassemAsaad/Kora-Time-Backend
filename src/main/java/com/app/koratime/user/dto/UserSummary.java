package com.app.koratime.user.dto;

import com.app.koratime.user.model.User;
import com.app.koratime.user.model.Role;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserSummary(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String profilePictureUrl,
        Role role,
        boolean emailVerified
) {
    public static UserSummary from(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
