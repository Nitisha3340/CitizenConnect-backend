package com.citizenconnect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.citizenconnect.entity.OtpPurpose;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.log-otp-to-console:false}")
    private boolean logOtpToConsole;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void logOtpForDev(String toEmail, String otp, String context) {
        if (logOtpToConsole) {
            log.warn("DEV ONLY — OTP for {} ({}): {}", toEmail, context, otp);
        }
    }

    public void sendOtp(String toEmail, String otp, OtpPurpose purpose) {
        String pass = mailPassword == null ? "" : mailPassword.replaceAll("\\s+", "");
        if (mailUsername == null || mailUsername.isBlank() || pass.isEmpty()) {
            throw new RuntimeException(
                    "Mail is not configured. With profile 'local', copy "
                            + "src/main/resources/application-mail-local.properties.example to "
                            + "application-mail-local.properties and set Gmail username + app password.");
        }

        String subject = purpose == OtpPurpose.LOGIN ? "Your login OTP" : "Verify your email";
        String body = "Your OTP is: " + otp + "\n\nThis code expires in 10 minutes.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailUsername);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("OTP email sent successfully to {}", toEmail);
            logOtpForDev(toEmail, otp, "SMTP accepted; check inbox/spam if missing");
        } catch (Exception ex) {
            log.error("SMTP failed sending OTP to {} (not swallowing — API will report error). Cause: {}", toEmail, ex.toString());
            logOtpForDev(toEmail, otp, "SMTP failed — use this code or fix mail config");
            throw new RuntimeException(
                    "Could not send verification email. Check Gmail app password, 2-Step Verification, "
                            + "and that port 587 is allowed. See server logs for details.",
                    ex);
        }
    }
}
