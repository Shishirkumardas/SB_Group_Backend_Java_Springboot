package org.sb_ibms.services;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class TwilioCallService {

    private static final String ACCOUNT_SID = "AC4a49f5afeedbd97bfa86c4d728908a48";
    private static final String AUTH_TOKEN = "46195fe35e1778e0b3b2f20a6934d2fd";
    private static final String TWILIO_NUMBER = "+8801617445356";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void callCustomer(String customerNumber) {
        Call.creator(
                new PhoneNumber(customerNumber), // To (BD number)
                new PhoneNumber(TWILIO_NUMBER),   // From (Twilio number)
                URI.create("https://example.com/twilio.xml")
        ).create();
    }
}
