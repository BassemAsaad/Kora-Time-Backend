package com.app.koratime.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendVerificationEmail(String to, String firstName, String token) {
        final String link = "http://localhost:8080/auth/verify-email?token=" + token;
        final String body = """
                Hi %s,
                
                Welcome to KoraTime! Please verify your email address by clicking the link below:
                
                %s
                
                This link expires in 24 hours.
                If you didn't create an account, you can safely ignore this email.
                
                — The KoraTime Team
                """.formatted(firstName, link);
        send(to, "Verify your email", body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String firstName, String token) {
        String link = "http://localhost:8080/auth/reset-password?token=" + token;
        String body = """
                Hi %s,
                
                We received a request to reset your KoraTime password.
                Click the link below to set a new password:
                
                %s
                
                This link expires in 1 hour.
                If you didn't request a password reset, you can safely ignore this email.
                
                — The KoraTime Team
                """.formatted(firstName, link);

        send(to, "Reset your KoraTime password", body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@koratime.com");
            javaMailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
