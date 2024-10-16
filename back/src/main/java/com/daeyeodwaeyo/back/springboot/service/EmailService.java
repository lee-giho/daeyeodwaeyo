package com.daeyeodwaeyo.back.springboot.service;

import jakarta.mail.*;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender; // Spring Boot의 JavaMailSener 주입

  // 인증 코드를 포함한 이메일을 전송하는 메서드
  public void sendVerificationEmail(String recipient, String code) throws MessagingException {
//    MimeMessage message = mailSender.createMimeMessage();
//    MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//    helper.setTo(recipient); // 수신자 설정
//    helper.setSubject("Verification Code"); // 이메일 제목 설정
//    helper.setText("Your verification code is: " + code, true); // 이메일 내용 설정
//
//    mailSender.send(message); // 이메일 전송
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom("dlrlghproject0429@gmail.com");
    message.setTo(recipient);
    message.setText("Your verification code is: " + code);
    message.setSubject("Verification Code");

    mailSender.send(message);
    System.out.println("Mail Sent successfully...");
  }

  // 4자리 인증 코드를 생성하는 메서드
  public String generateVerificationCode() {
    return String.valueOf((int)(Math.random() * 9000) + 1000); // 1000 ~ 9999의 난수 생성
  }
}
