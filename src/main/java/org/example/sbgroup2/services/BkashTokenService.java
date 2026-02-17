package org.example.sbgroup2.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
//Generate Token will simply create tokens using the url,username,password and other things that bkash will send.
@Service
public class BkashTokenService {

    @Value("${bkash.base-url}")
    private String baseUrl;

    @Value("${bkash.username}")
    private String username;

    @Value("${bkash.password}")
    private String password;

    @Value("${bkash.app-key}")
    private String appKey;

    @Value("${bkash.app-secret}")
    private String appSecret;

    private String token;
    private LocalDateTime expiry;

    // In BkashTokenService.java
    public String getToken() {
        if (token != null && expiry.isAfter(LocalDateTime.now())) {
            return token;
        }
        return generateToken();
    }

    private String generateToken() {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("username", username);
        headers.set("password", password);

        Map<String, String> body = new HashMap<>();
        body.put("app_key", appKey);
        body.put("app_secret", appSecret);

        System.out.println("Request Body: " + body);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/tokenized-checkout/auth/grant-token",
                entity,
                Map.class
        );

        Map<String, Object> result = response.getBody();
        System.out.println("Generate Token Success: " + result);

        token = (String) response.getBody().get("id_token");
        expiry = LocalDateTime.now().plusMinutes(55);

        return token;
    }
}

