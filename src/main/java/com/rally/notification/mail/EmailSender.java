package com.rally.notification.mail;

public interface EmailSender {
    void send(String to, String subject, String htmlBody);
}
