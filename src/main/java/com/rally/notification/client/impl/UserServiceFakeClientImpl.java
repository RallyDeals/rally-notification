package com.rally.notification.client.impl;

import com.rally.notification.client.UserServiceClient;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("dev")
@Component
public class UserServiceFakeClientImpl implements UserServiceClient {

    @Override
    public String getUserEmail(UUID userId) {
        String email = "user-" + userId + "@example.com";
        log.debug("Fake user service resolved email {} for userId={}", email, userId);
        return email;
    }
}