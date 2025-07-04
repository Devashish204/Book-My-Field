package com.mit.project.dtos;

import lombok.Data;

@Data
public class OtpVerificationRequest {
  private String email;
  private String userOtp;
}
