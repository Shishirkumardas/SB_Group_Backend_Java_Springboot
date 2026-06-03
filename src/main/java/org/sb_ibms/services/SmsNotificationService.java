package org.sb_ibms.services;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.models.ShoppingMallCustomer;
import org.sb_ibms.repositories.ShoppingMallCustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsNotificationService {

    private final ShoppingMallCustomerRepository customerRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${bulksmsbd.api.key}")
    private String apiKey;

    @Value("${bulksmsbd.sender.id}")
    private String senderId;

    /**
     * Send SMS to ALL registered customers
     */
    public void sendToAllCustomers(String message) {
        List<ShoppingMallCustomer> customers = customerRepository.findAll();

        int success = 0, failed = 0;

        for (ShoppingMallCustomer customer : customers) {
            if (customer.getPhone() != null) {
                String phone = normalizePhone(customer.getPhone().toBigInteger().toString());
                boolean sent = sendSingleSms(phone, message);
                if (sent) success++;
                else failed++;
            }
        }

        System.out.println("✅ SMS Campaign Completed | Success: " + success + " | Failed: " + failed);
    }

    /**
     * Send SMS to a specific customer
     */
    public void sendSmsToCustomer(Long customerId, String message) {
        ShoppingMallCustomer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        String phone = normalizePhone(customer.getPhone().toBigInteger().toString());
        sendSingleSms(phone, message);
    }

    /**
     * Send single SMS
     */
    private boolean sendSingleSms(String phoneNumber, String message) {
        try {
            String url = "http://bulksmsbd.net/api/smsapi";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("api_key", apiKey);
            body.put("senderid", senderId);
            body.put("number", phoneNumber);
            body.put("message", message);
            body.put("type", "text");   // or "unicode" for Bangla

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            System.out.println("SMS to " + phoneNumber + " → " + response.getBody());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("Failed to send SMS to " + phoneNumber + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Clean phone number (remove 0, +88 etc.)
     */
    private String normalizePhone(String phone) {
        phone = phone.replaceAll("[^0-9]", ""); // remove non-digits
        if (phone.startsWith("88")) phone = phone.substring(2);
        if (phone.startsWith("0")) phone = phone.substring(1);
        return "88" + phone;   // Bulk SMS BD usually expects 8801xxxxxxxxx
    }
}
