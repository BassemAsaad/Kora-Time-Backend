package com.app.koratime.auth.service;

import com.app.koratime.auth.dto.*;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void verifyEmail(VerifyEmailRequest request);

    void resendVerificationEmail(String email);

    void forgetPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

}
