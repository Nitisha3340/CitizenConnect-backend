package com.citizenconnect.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ ADD THIS

import com.citizenconnect.entity.Otp;
import com.citizenconnect.entity.User;
import com.citizenconnect.repository.OtpRepository;
import com.citizenconnect.repository.UserRepository;

@Service
public class OtpService {
	

	@Autowired
	private UserRepository userRepo;
	@Autowired
	private OtpRepository repo;
	@Autowired
	private EmailService emailService;
	
	@Transactional
	public String generateOtp(String email) {

	    repo.deleteByEmail(email);

	    String otp = String.valueOf(100000 + new Random().nextInt(900000));

	    Otp otpEntity = new Otp();
	    otpEntity.setEmail(email);
	    otpEntity.setOtpCode(otp);
	    otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

	    repo.save(otpEntity);

	    // ✅ SEND EMAIL
	    emailService.sendOtp(email, otp);

	    return "OTP sent to your email";
	}

	@Transactional
	public boolean verifyOtp(String email, String otp) {

	    Otp storedOtp = repo.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("OTP not found"));

	    if (storedOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
	        throw new RuntimeException("OTP expired");
	    }

	    if (!storedOtp.getOtpCode().equals(otp)) {
	        throw new RuntimeException("Invalid OTP");
	    }

	    // ✅ mark user verified
	    User user = userRepo.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    user.setVerified(true);
	    userRepo.save(user);

	    // delete OTP
	    repo.deleteByEmail(email);

	    return true;
	    
	    
	}
}