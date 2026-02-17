package org.example.sbgroup2.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.sbgroup2.models.MasterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BkashPaymentService {

    @Autowired
    private BkashTokenService tokenService;
    @Autowired
    private MasterDataService masterDataService;

    @Value("${bkash.base-url}")
    private String baseUrl;

    @Value("${bkash.callback-url}")
    private String callbackUrl;

    @Value("${bkash.app-key}")
    private String appKey;
    @Autowired
    private PaymentService paymentService;

    //This will not take any ID, Just amount and invoice, but for paying to each id we need to keep trac
    //So the second method makePayment is more suitable
    public Map<String, Object> createPayment(BigDecimal amount, String invoice) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(tokenService.getToken());
        headers.set("X-App-Key", appKey);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount.toString());
        body.put("currency", "BDT");
        body.put("intent", "sale");
        body.put("merchantInvoiceNumber", invoice);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/tokenized/checkout/create",
                entity,
                Map.class
        );

        return response.getBody();
    }

    //Cashback Payment //Payment for each master data //customerPayment
    // In BkashPaymentService.java
    public Map<String, Object> makePayment(Long masterDataId) {

        MasterData md = masterDataService.getMasterDataById(masterDataId);
        BigDecimal amount = md.getPurchaseAmount();

        String invoice = "MD-" + masterDataId + "-" + System.currentTimeMillis();

        paymentService.createPendingPayment(md, amount, invoice);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // IMPORTANT — NO Bearer prefix
        headers.set("Authorization", tokenService.getToken());
        headers.set("X-App-Key", appKey);

        Map<String, Object> body = new HashMap<>();
        body.put("mode", "0011"); // REQUIRED
        body.put("payerReference", "01770618575"); // REQUIRED
        body.put("callbackURL", "http://localhost:3001/customer/pay/"+masterDataId.toString()+"/callback"); // REQUIRED
        body.put("amount", amount.toString());
        body.put("currency", "BDT");
        body.put("intent", "sale");
        body.put("merchantInvoiceNumber", invoice);

        System.out.println("Request Body: " + body);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/tokenized-checkout/payment/create",
                entity,
                Map.class
        );

        Map<String, Object> result = response.getBody();
        System.out.println("Create Payment Success: " + result);
        return result;

    }



    public Map<String, Object> executePayment(String paymentID) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.set("authorization", "Bearer " + tokenService.getToken());
        headers.set("x-app-key", appKey);

        Map<String, String> body = new HashMap<>();
        body.put("paymentId", paymentID);

        System.out.println("Request Body: " + body);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/tokenized-checkout/payment/execute",
                    entity,
                    Map.class
            );

            Map<String, Object> result = response.getBody();
            System.out.println("Execute Payment Success: " + result);
            return result;

        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            System.err.println("bKash execute response: " + e.getStatusCode() + " → " + errorBody);

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> errorMap = new ObjectMapper().readValue(errorBody, Map.class);
                String internalCode = (String) errorMap.get("internalCode");
                String errorMessageEn = (String) errorMap.get("errorMessageEn");

                // Treat "already completed" as SUCCESS (most common idempotency pattern)
                if ("payment_already_completed".equals(internalCode) ||
                        "The payment has already been completed".equals(errorMessageEn) ||
                        "2062".equals(errorMap.get("externalCode"))) {

                    System.out.println("Payment was already completed → treating as success");

                    // Return a fake success-like response so frontend can show success
                    Map<String, Object> fakeSuccess = new HashMap<>();
                    fakeSuccess.put("paymentId", paymentID);
                    fakeSuccess.put("transactionStatus", "Completed");
                    fakeSuccess.put("trxId", "ALREADY_COMPLETED_" + paymentID); // or query it
                    fakeSuccess.put("amount", "N/A (already processed)");
                    fakeSuccess.put("message", "Payment already completed successfully");
                    return fakeSuccess;
                }

                // For all other errors → rethrow
                throw new RuntimeException("bKash Execute failed: " + errorMessageEn);

            } catch (Exception parseEx) {
                throw new RuntimeException("Failed to parse bKash error: " + errorBody);
            }
        }
    }

    public Map<String, Object> queryPayment(String paymentId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("authorization", "Bearer " + tokenService.getToken());
        headers.set("x-app-key", appKey);

        Map<String, String> body = new HashMap<>();
        body.put("paymentId", paymentId);

        System.out.println("Request Body: " + body);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/tokenized-checkout/query/payment",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = response.getBody();
                System.out.println("Query Payment Success: " + result);
                return result;
            } else {
                throw new RuntimeException("Query Payment failed: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            System.err.println("bKash Query Payment error: " + e.getStatusCode() + " - " + errorBody);
            try {
                Map<String, Object> errorMap = new ObjectMapper().readValue(errorBody, Map.class);
                String message = (String) errorMap.getOrDefault("errorMessageEn", "Unknown error");
                throw new RuntimeException("bKash Query Payment failed: " + message);
            } catch (Exception ex) {
                throw new RuntimeException("bKash Query Payment failed: " + errorBody);
            }
        }
    }

    public Map<String, Object> searchTransaction(String trxId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("authorization", "Bearer " + tokenService.getToken());
        headers.set("x-app-key", appKey);

        Map<String, String> body = new HashMap<>();
        body.put("trxId", trxId);
        System.out.println("Request Body: " + body);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/tokenized-checkout/general/search-transaction",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = response.getBody();
                System.out.println("Search Transaction Success: " + result);
                return result;
            } else {
                throw new RuntimeException("Search Transaction failed: " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            System.err.println("bKash Search Transaction error: " + e.getStatusCode() + " - " + errorBody);
            try {
                Map<String, Object> errorMap = new ObjectMapper().readValue(errorBody, Map.class);
                String message = (String) errorMap.getOrDefault("errorMessageEn", "Unknown error");
                throw new RuntimeException("bKash Search Transaction failed: " + message);
            } catch (Exception ex) {
                throw new RuntimeException("bKash Search Transaction failed: " + errorBody);
            }
        }
    }

}

