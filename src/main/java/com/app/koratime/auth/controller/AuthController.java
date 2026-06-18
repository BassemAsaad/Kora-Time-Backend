package com.app.koratime.auth.controller;

import com.app.koratime.auth.dto.*;
import com.app.koratime.auth.service.AuthService;
import com.app.koratime.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Register, login, email verification, password reset")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "register a new account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse
                        .success("Registration successful. Check your email to verify your account.", response)
                );
    }

    @PostMapping("/login")
    @Operation(summary = "login to account")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Use refresh token to get a new access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity
                .ok(ApiResponse.success(response));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email address using the token sent by email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity
                .ok(ApiResponse.success("Email verified successfully. You can now use all features."));
    }

    @PostMapping("/resend-verification-email")
    @Operation(summary = "Resend email verification link")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Verification email is sent. Check your inbox."));
    }

    @PostMapping("/forget-password")
    @Operation(summary = "Request password reset link")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgetPassword(request);
        return ResponseEntity
                .ok(ApiResponse.success("If an account with that email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using the token sent by email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity
                .ok(ApiResponse.success("Password is reset successfully. You can now login with the new password."));
    }
}
