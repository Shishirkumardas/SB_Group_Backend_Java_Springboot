package org.sb_ibms.controller;

import org.sb_ibms.services.TwilioCallService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calls")
public class CallController {

    private final TwilioCallService callService;

    public CallController(TwilioCallService callService) {
        this.callService = callService;
    }

    @PostMapping("/call")
    public void call(@RequestParam String phone) {
        callService.callCustomer(phone);
    }
}
