package com.mit.project.services.otp;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OptService {
  private static final String OTP_PREFIX = "OTP_";
  @Autowired private StringRedisTemplate redisTemplate;

  // Generate and store OPT in redis

  public String generateOtp(String email) {
    String otp = String.valueOf(new Random().nextInt(900000) + 100000); // 6 digit otp
    redisTemplate
        .opsForValue()
        .set(OTP_PREFIX + email, otp, 5, TimeUnit.MINUTES); // expires in 5 min
    return otp;
  }

  // verify otp

  public boolean verifyOtp(String email, String userOpt) {
    String storedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + email);
    return storedOtp != null && storedOtp.equals(userOpt);
  }

  // delete otp after successful verification

  public void deleteOtp(String email) {
    redisTemplate.delete(OTP_PREFIX + email);
  }
}
