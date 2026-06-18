package com.app.koratime.auth.service;

import com.app.koratime.auth.dto.*;
import com.app.koratime.auth.model.EmailVerificationToken;
import com.app.koratime.auth.model.PasswordResetToken;
import com.app.koratime.auth.repo.EmailVerificationTokenRepo;
import com.app.koratime.auth.repo.PasswordResetTokenRepo;
import com.app.koratime.common.exception.BusinessViolatedException;
import com.app.koratime.common.exception.DuplicateResourceException;
import com.app.koratime.common.exception.ResourceNotFoundException;
import com.app.koratime.common.security.JwtService;
import com.app.koratime.common.security.UserPrincipal;
import com.app.koratime.email.EmailService;
import com.app.koratime.user.dto.UserSummary;
import com.app.koratime.user.factory.UserFactory;
import com.app.koratime.user.model.User;
import com.app.koratime.user.model.Role;
import com.app.koratime.user.repo.ManagerProfileRepo;
import com.app.koratime.user.repo.PlayerProfileRepo;
import com.app.koratime.user.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepo userRepo;
    private final ManagerProfileRepo managerProfileRepo;
    private final PlayerProfileRepo playerProfileRepo;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Received request to register a new account {}", request);

        if (request.role().equals(Role.ADMIN)) {
            throw new BusinessViolatedException("Admin accounts cannot be self-registered");
        }

        if (userRepo.existsByEmail(request.email())){
            throw new DuplicateResourceException("User", "email", request.email());
        }

        if (userRepo.existsByPhoneNumber(request.phoneNumber())){
            throw new DuplicateResourceException("User", "phoneNumber", request.phoneNumber());
        }

        if (request.role().equals(Role.MANAGER)){

            if (request.nationalId() == null || request.nationalId().isBlank()) {
                throw new BusinessViolatedException("National ID is required for manager accounts");
            }

            if (managerProfileRepo.existsByNationalId(request.nationalId())) {
                throw new DuplicateResourceException("Manager", "nationalId", request.nationalId());
            }
        }

        User user = UserFactory.buildUser(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.phoneNumber(),
                request.role()
        );
        User savedUser = userRepo.save(user);
        log.info("New user registered. id {}, role {}.", savedUser.getId(), savedUser.getRole());

        switch (request.role()) {
            case MANAGER -> managerProfileRepo.save(UserFactory.buildManagerProfile(user, request.nationalId()));
            case PLAYER -> playerProfileRepo.save(UserFactory.buildPlayerProfile(user));
        }

        String token = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = EmailVerificationToken.builder()
                .user(savedUser)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        EmailVerificationToken savedEmailToken = emailVerificationTokenRepo.save(emailToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFirstName(), savedEmailToken.getToken());

        return buildAuthResponse(user, null);
    }

    @Override
    public AuthResponse login(@NonNull LoginRequest request) {

        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepo
                .findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId().toString()));
        log.info("User {} logged in successfully.", user.getId());

        return buildAuthResponse(user, null);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Received request to refresh token {}", request);

        if (!jwtService.isRefreshToken(request.refreshToken())) {
            log.info("Invalid refresh token.");
            throw new BusinessViolatedException("Provided token is not a refresh token");
        }

        if (!jwtService.isTokenValid(request.refreshToken())) {
            log.info("RefreshToken not valid.");
            throw new  BusinessViolatedException("Refresh token is invalid or expired");
        }

        String email = jwtService.extractEmail(request.refreshToken());
        log.info("Email {}.", email);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isBanned()) {
            log.info("Banned user {}.", user.getId());
            throw new BusinessViolatedException("Account is suspended");
        }

        return buildAuthResponse(user, request.refreshToken());
    }

    @Transactional
    @Override
    public void verifyEmail(VerifyEmailRequest request) {
        log.info("verifying email");

        EmailVerificationToken verifyToken = emailVerificationTokenRepo
                .findByToken(request.token())
                .orElseThrow(() -> new BusinessViolatedException("Verification link is invalid"));

        if (verifyToken.isUsed()) {
            log.info("Verification link has already been used");
            throw new BusinessViolatedException("Verification link has expired or was already used. Request a new one.");
        }
        verifyToken.setUsed(true);
        emailVerificationTokenRepo.save(verifyToken);

        User user = verifyToken.getUser();
        user.setEmailVerified(true);
        userRepo.save(user);

        log.info("Email is verified for user {}",user.getId());
    }

    @Transactional
    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isEmailVerified()) {
            throw new BusinessViolatedException("Email is already verified");
        }

        emailVerificationTokenRepo.invalidateAllTokens(user.getId());
        String token = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        EmailVerificationToken savedEmailToken = emailVerificationTokenRepo.save(emailToken);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), savedEmailToken.getToken());

        log.info("Email verification code is sent to user: {}", user.getEmail());
    }

    @Transactional
    @Override
    public void forgetPassword(ForgotPasswordRequest request) {
        userRepo.findByEmail(request.email())
                .ifPresent(u -> {
                    passwordResetTokenRepo.invalidateAllTokens(u.getId());

                    String token = UUID.randomUUID().toString();
                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .user(u)
                            .token(token)
                            .expiresAt(LocalDateTime.now().plusHours(1))
                            .build();

                    PasswordResetToken saved = passwordResetTokenRepo.save(resetToken);
                    emailService.sendPasswordResetEmail(u.getEmail(), u.getFirstName(), saved.getToken());
                });
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepo
                .findByToken(request.token())
                .orElseThrow(() -> new ResourceNotFoundException("Reset link is invalid"));

        if (resetToken.isUsed()) {
            throw new BusinessViolatedException("Reset link has expired or was already used. Request a new one.");
        }

        resetToken.setUsed(true);
        passwordResetTokenRepo.save(resetToken);

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);

        log.info("Password is reset for user {}", user.getId());
    }

    private AuthResponse buildAuthResponse(User user, String refreshToken) {
        UserPrincipal principal = UserPrincipal.from(user);

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(principal))
                .refreshToken(refreshToken == null ? jwtService.generateRefreshToken(principal) : refreshToken)
                .user(UserSummary.from(user))
                .build();
    }
}
