package com.rally.notification.client.impl;


import com.rally.common.exceptions.shared.InternalServerErrorException;
import com.rally.common.exceptions.shared.ServiceUnavailableException;
import com.rally.notification.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {
    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Override
    public String getUserEmail(UUID userId) {
        String url = userServiceUrl + "/users/" + userId.toString();

        try{
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        }catch (ResourceAccessException resourceAccessException){
            throw new ServiceUnavailableException("User service is unavailable");
        } catch(HttpServerErrorException serverErrorException){
            throw new InternalServerErrorException("User service returned server error");
        }
    }
}
