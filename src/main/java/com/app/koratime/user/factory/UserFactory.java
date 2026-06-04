package com.app.koratime.user.factory;

import com.app.koratime.user.model.*;

public final class UserFactory {

    private UserFactory() {}

    public static User buildUser(
            String email, String password, String firstName, String lastName, String phoneNumber, UserRole role) {
        return User.builder()
                .email(email)
                .password(password)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .role(role)
                .build();
    }

    public static PlayerProfile buildPlayerProfile(User user) {
        return PlayerProfile.builder()
                .user(user)
                .build();
    }

    public static ManagerProfile buildManagerProfile(User user, String nationalId) {
        return ManagerProfile.builder()
                .user(user)
                .nationalId(nationalId)
                .build();
    }

    public AdminProfile buildAdminProfile(User user) {
        return AdminProfile.builder()
                .user(user)
                .build();
    }

}
