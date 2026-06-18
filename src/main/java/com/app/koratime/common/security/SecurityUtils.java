package com.app.koratime.common.security;

import com.app.koratime.common.exception.BusinessViolatedException;
import com.app.koratime.user.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BusinessViolatedException("No authenticated user found in security context");
        }

        return principal;
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static boolean isCurrentUserRule(Role role) {
        return Objects.equals(getCurrentUser().getRole(), role);
    }
}
