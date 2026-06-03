package org.sb_ibms.controller;

import lombok.RequiredArgsConstructor;
import org.sb_ibms.services.SmsNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsNotificationService smsService;

    @PostMapping("/send-to-all")
    public ResponseEntity<String> sendToAll(@RequestBody String message) {
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message cannot be empty");
        }
        smsService.sendToAllCustomers(message.trim());
        return ResponseEntity.ok("✅ SMS campaign started successfully!");
    }
}
