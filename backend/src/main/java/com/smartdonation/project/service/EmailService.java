package com.smartdonation.project.service;

import com.resend.core.exception.ResendException;

/** Sends transactional emails (verification OTPs) via Resend. */
public interface EmailService {

    void sendOtpEmail(String toEmail, String otp) throws ResendException;
}
