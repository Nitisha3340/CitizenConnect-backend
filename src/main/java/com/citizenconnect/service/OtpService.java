package com.citizenconnect.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.citizenconnect.entity.Otp;
import com.citizenconnect.entity.OtpPurpose;
import com.citizenconnect.repository.OtpRepository;
import com.citizenconnect.repository.UserRepository;

@Service
public class OtpService {

    private final UserRepository userRepo;
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    /** Minimum seconds between OTP sends per email+purpose. Set 0 to disable (e.g. local testing). */
    @Value("${app.otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    public OtpService(UserRepository userRepo, OtpRepository otpRepository, EmailService emailService) {
        this.userRepo = userRepo;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void sendLoginOtp(String email) {
        userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        createAndSend(email, OtpPurpose.LOGIN);
    }

    private void createAndSend(String email, OtpPurpose purpose) {
        LocalDateTime now = LocalDateTime.now();
        if (resendCooldownSeconds > 0) {
            otpRepository.findByEmailAndPurpose(email, purpose).ifPresent(existing -> {
                LocalDateTime sentAt = existing.getCreatedAt();
                if (sentAt == null) {
                    sentAt = existing.getExpiryTime().minusMinutes(otpExpiryMinutes);
                }
                if (sentAt.plusSeconds(resendCooldownSeconds).isAfter(now)) {
                    throw new RuntimeException("Please wait before requesting another OTP");
                }
            });
        }

        otpRepository.deleteByEmailAndPurpose(email, purpose);

        String code = String.valueOf(100000 + new Random().nextInt(900000));
        Otp entity = new Otp();
        entity.setEmail(email);
        entity.setPurpose(purpose);
        entity.setOtpCode(code);
        entity.setCreatedAt(now);
        entity.setExpiryTime(now.plusMinutes(otpExpiryMinutes));
        otpRepository.save(entity);

        emailService.sendOtp(email, code, purpose);
    }

    @Transactional
    public void verifyLoginOtp(String email, String otp) {
        Otp stored = otpRepository.findByEmailAndPurpose(email, OtpPurpose.LOGIN)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (stored.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.deleteByEmailAndPurpose(email, OtpPurpose.LOGIN);
            throw new RuntimeException("OTP expired");
        }

        if (!stored.getOtpCode().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        otpRepository.deleteByEmailAndPurpose(email, OtpPurpose.LOGIN);
    }
}
