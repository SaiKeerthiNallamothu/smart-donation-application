package com.smartdonation.project.service.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.smartdonation.project.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final Resend resendClient;

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otp) throws ResendException {
        // Until a RESEND_API_KEY is configured, print the OTP to the console instead.
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("RESEND_API_KEY not configured - printing OTP to console for email: {}", toEmail);
            System.out.println("==============================================");
            System.out.println("VERIFICATION OTP for " + toEmail + " : " + otp);
            System.out.println("==============================================");
            return;
        }

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject("Smart Donation - Email Verification")
                .html("<p>Your Smart Donation verification code is:</p>"
                        + "<h2 style=\"letter-spacing:4px;\">" + otp + "</h2>"
                        + "<p>It is valid for 5 minutes. Do not share it with anyone.</p>")
                .build();

        CreateEmailResponse data = resendClient.emails().send(options);
        log.info("Verification OTP email sent to: {} (message id: {})", toEmail, data.getId());
    }
}
