package com.mit.project.services.otp;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailService {

  @Autowired private JavaMailSender mailSender;
  @Autowired private SpringTemplateEngine templateEngine;

  public void sendOtp(String email, String otp) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message);

      helper.setFrom("no-reply@bmf.com");
      helper.setTo(email);
      helper.setSubject("Email Verification Code");
      helper.setText("Your Code is: " + otp + ". It is valid for 5 minutes only.");


    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }

  public void sendMail(Context context, String templateFileName, String toEmail, String subject) {
    String body = templateEngine.process("fragments/" + templateFileName, context);

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message);

      helper.setFrom("no-reply@bmf.com");
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(body, true);

      mailSender.send(message);
    } catch (MessagingException e) {
      e.printStackTrace();
    }
  }
}
