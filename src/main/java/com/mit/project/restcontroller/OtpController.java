package com.mit.project.restcontroller;

import com.mit.project.dtos.OtpRequest;
import com.mit.project.dtos.OtpVerificationRequest;
import com.mit.project.services.otp.EmailService;
import com.mit.project.services.otp.OptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

  @Autowired private OptService otpService;

  @Autowired private EmailService emailService;

  // Generate and send OTP
  @PostMapping("/generate")
  public ResponseEntity<String> generateOtp(@RequestBody OtpRequest request) {
    String otp = otpService.generateOtp(request.getEmail());
    emailService.sendOtp(request.getEmail(), otp);
    return ResponseEntity.ok("OTP sent to email: " + request.getEmail());
  }

  // Verify OTP
  @PostMapping("/verify")
  public ResponseEntity<String> verifyOtp(@RequestBody OtpVerificationRequest request) {
    if (otpService.verifyOtp(request.getEmail(), request.getUserOtp())) {
      otpService.deleteOtp(request.getEmail());
      return ResponseEntity.ok("Email Verified Successfully!");
    }
    return ResponseEntity.status(400).body("Invalid or expired OTP.");
  }
}
