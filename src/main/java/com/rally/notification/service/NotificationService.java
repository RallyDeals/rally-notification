package com.rally.notification.service;

import com.rally.notification.client.UserServiceClient;
import com.rally.notification.mail.EmailSender;
import com.rally.notification.messaging.event.DealOrderCancelled;
import com.rally.notification.messaging.event.EmailVerificationRequested;
import com.rally.notification.messaging.event.NormalOrderCancelled;
import com.rally.notification.messaging.event.OrderAuthorized;
import com.rally.notification.messaging.event.OrderCreated;
import com.rally.notification.messaging.event.PasswordResetRequested;
import com.rally.notification.messaging.event.UserRegistered;
import com.rally.notification.security.OtpDecryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserServiceClient userServiceClient;
    private final EmailSender emailSender;
    private final SpringTemplateEngine templateEngine;
    private final OtpDecryptor otpDecryptor;

    public void notifyUserRegistered(UserRegistered event) {
        log.info("Handling User.Registered notification for userId={}, email={}, role={}",
                event.userId(), maskEmail(event.email()), event.role());
        Context context = new Context();
        context.setVariable("email", event.email());
        context.setVariable("role", event.role());
        sendEmail(event.email(), "Welcome to Rally!", "email/user-registered", context);
    }

    public void notifyEmailVerificationRequested(EmailVerificationRequested event) {
        log.info("Handling User.EmailVerificationRequested notification for userId={}, email={}",
                event.userId(), maskEmail(event.email()));
        Context context = new Context();
        context.setVariable("otp", otpDecryptor.decrypt(event.otp()));
        sendEmail(event.email(), "Verify your email", "email/email-verification", context);
    }

    public void notifyPasswordResetRequested(PasswordResetRequested event) {
        log.info("Handling User.PasswordResetRequested notification for userId={}, email={}",
                event.userId(), maskEmail(event.email()));
        Context context = new Context();
        context.setVariable("otp", otpDecryptor.decrypt(event.otp()));
        sendEmail(event.email(), "Reset your password", "email/password-reset", context);
    }

    public void notifyOrderCreated(OrderCreated event) {
        log.info("Handling Order.Created notification for orderId={}, userId={}, totalPrice={}",
                event.orderId(), event.userId(), event.totalPrice());
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("items", event.items());
        context.setVariable("totalPrice", event.totalPrice());
        context.setVariable("address", event.address());
        sendEmail(event.userId(), "Your order has been created", "email/order-created", context);
    }

    public void notifyOrderAuthorized(OrderAuthorized event) {
        log.info("Handling Order.Authorized notification for orderId={}, dealId={}, userId={}, totalPrice={}",
                event.orderId(), event.dealId(), event.userId(), event.totalPrice());
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("dealId", event.dealId());
        context.setVariable("totalPrice", event.totalPrice());
        sendEmail(event.userId(), "Your join has been authorized", "email/order-authorized", context);
    }

    public void notifyDealOrderCancelled(DealOrderCancelled event) {
        log.info("Handling Order.DealCancelled notification for orderId={}, dealId={}, userId={}, reason={}",
                event.orderId(), event.dealId(), event.userId(), event.reason());
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("dealId", event.dealId());
        context.setVariable("reason", event.reason());
        context.setVariable("items", event.items());
        context.setVariable("totalPrice", event.totalPrice());
        sendEmail(event.userId(), "Your deal order has been cancelled", "email/order-deal-cancelled", context);
    }

    public void notifyNormalOrderCancelled(NormalOrderCancelled event) {
        log.info("Handling Order.NormalCancelled notification for orderId={}, userId={}, cancelReason={}",
                event.orderId(), event.userId(), event.cancelReason());
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("cancelReason", event.cancelReason());
        context.setVariable("items", event.items());
        context.setVariable("totalPrice", event.totalPrice());
        context.setVariable("paymentErrorMessage", event.paymentErrorMessage());
        sendEmail(event.userId(), "Your order has been cancelled", "email/order-normal-cancelled", context);
    }

    private void sendEmail(UUID userId, String subject, String template, Context context) {
        log.debug("Resolving email address for userId={}", userId);
        String email = userServiceClient.getUserEmail(userId);
        sendEmail(email, subject, template, context);
    }

    private void sendEmail(String email, String subject, String template, Context context) {
        log.info("Rendering email template {} for recipient {} with subject '{}'", template, maskEmail(email), subject);
        try {
            String htmlBody = templateEngine.process(template, context);
            emailSender.send(email, subject, htmlBody);
            log.info("Email sent: recipient={}, subject='{}', template={}", maskEmail(email), subject, template);
        } catch (Exception e) {
            log.error("Failed to send email: recipient={}, subject='{}', template={}",
                    maskEmail(email), subject, template, e);
            throw e;
        }
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}