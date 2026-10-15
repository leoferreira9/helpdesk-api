package com.leonardo.helpdesk.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final String from;
    private final String baseUrl;

    public EmailService(
            JavaMailSender javaMailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.base-url}") String baseUrl
    ) {
        this.javaMailSender = javaMailSender;
        this.from = from;
        this.baseUrl = baseUrl;
    }

    public void sendVerificationEmail(String recipient, String token) {
        String url = baseUrl + "/verify-email?token=" + token;

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(recipient);
        simpleMailMessage.setSubject("Confirm your registration!");
        simpleMailMessage.setText("Please click the link to confirm your registration!\n" + url);

        javaMailSender.send(simpleMailMessage);
    }
}
