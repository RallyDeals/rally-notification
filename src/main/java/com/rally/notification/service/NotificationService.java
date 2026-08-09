package com.rally.notification.service;

import com.rally.notification.client.UserServiceClient;
import com.rally.notification.mail.EmailSender;
import com.rally.notification.messaging.event.DealOrderCancelled;
import com.rally.notification.messaging.event.NormalOrderCancelled;
import com.rally.notification.messaging.event.OrderAuthorized;
import com.rally.notification.messaging.event.OrderCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final UserServiceClient userServiceClient;
    private final EmailSender emailSender;
    private final SpringTemplateEngine templateEngine;

    public void notifyOrderCreated(OrderCreated event) {
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("items", event.items());
        context.setVariable("totalPrice", event.totalPrice());
        context.setVariable("address", event.address());
        sendEmail(event.userId(), "Your order has been created", "email/order-created", context);
    }

    public void notifyOrderAuthorized(OrderAuthorized event) {
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("dealId", event.dealId());
        context.setVariable("totalPrice", event.totalPrice());
        sendEmail(event.userId(), "Your join has been authorized", "email/order-authorized", context);
    }

    public void notifyDealOrderCancelled(DealOrderCancelled event) {
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("dealId", event.dealId());
        context.setVariable("reason", event.reason());
        context.setVariable("items", event.items());
        context.setVariable("totalPrice", event.totalPrice());
        sendEmail(event.userId(), "Your deal order has been cancelled", "email/order-deal-cancelled", context);
    }

    public void notifyNormalOrderCancelled(NormalOrderCancelled event) {
        Context context = new Context();
        context.setVariable("orderId", event.orderId());
        context.setVariable("cancelReason", event.cancelReason());
        context.setVariable("items", event.items());
        context.setVariable("totalPrice", event.totalPrice());
        context.setVariable("paymentErrorMessage", event.paymentErrorMessage());
        sendEmail(event.userId(), "Your order has been cancelled", "email/order-normal-cancelled", context);
    }

    private void sendEmail(UUID userId, String subject, String template, Context context) {
        String email = userServiceClient.getUserEmail(userId);
        String htmlBody = templateEngine.process(template, context);
        emailSender.send(email, subject, htmlBody);
    }
}
