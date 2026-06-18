package com.app.koratime.email;

public interface EmailService {

    void sendVerificationEmail(String to, String firstName, String token);

    void sendPasswordResetEmail(String to, String firstName, String token);
}
