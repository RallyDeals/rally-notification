package com.rally.notification.mail.impl;

import com.rally.common.exceptions.shared.InternalServerErrorException;
import com.rally.notification.mail.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {
    private final JavaMailSender mailSender;

    @Value("${notification.mail.from:no-reply@rally.com}")
    private String fromAddress;

    @Override
    public void send(String to, String subject, String htmlBody) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
        } catch (MessagingException e) {
            throw new InternalServerErrorException("Failed to build notification email");
        }
        mailSender.send(message);
    }
}
