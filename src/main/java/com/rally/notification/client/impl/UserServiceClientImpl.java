package com.rally.notification.client.impl;


import com.rally.common.exceptions.shared.InternalServerErrorException;
import com.rally.common.exceptions.shared.ServiceUnavailableException;
import com.rally.notification.client.UserServiceClient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Profile("prod")
@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {
    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Override
    public String getUserEmail(UUID userId) {
        String url = userServiceUrl + "/users/" + userId;
        log.info("Resolving email for userId={} from user service: {}", userId, url);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.debug("User service responded for userId={} with status {}", userId, response.getStatusCode());
            return response.getBody();
        } catch (ResourceAccessException resourceAccessException) {
            log.error("User service unreachable while resolving email for userId={}: {}", userId, resourceAccessException.getMessage());
            throw new ServiceUnavailableException("User service is unavailable");
        } catch (HttpServerErrorException serverErrorException) {
            log.error("User service returned server error while resolving email for userId={}: status={}",
                    userId, serverErrorException.getStatusCode(), serverErrorException);
            throw new InternalServerErrorException("User service returned server error");
        }
    }
}