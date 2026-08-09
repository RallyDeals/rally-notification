package com.rally.notification.client.impl;

import com.rally.notification.client.UserServiceClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Profile("dev")
@Component
public class UserServiceFakeClientImpl implements UserServiceClient {

    @Override
    public String getUserEmail(UUID userId) {
        return "user-" + userId + "@example.com";
    }
}
