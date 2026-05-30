package org.sb_ibms.services;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class CurrentMallService {

    private static final String MALL_SESSION_KEY = "CURRENT_SHOPPING_MALL_ID";

    private final HttpSession httpSession;

    // Constructor injection (better than @Autowired field)
    public CurrentMallService(HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    public void setCurrentMall(Long shoppingMallId) {
        if (shoppingMallId != null) {
            httpSession.setAttribute(MALL_SESSION_KEY, shoppingMallId);
            System.out.println("✅ SUCCESS: Mall ID " + shoppingMallId + " saved in session");
        }
    }

    public Long getCurrentMallId() {
        Object value = httpSession.getAttribute(MALL_SESSION_KEY);
        System.out.println("🔍 Session read - Mall ID: " + value);
        return (value instanceof Long) ? (Long) value : null;
    }

    public void clearCurrentMall() {
        httpSession.removeAttribute(MALL_SESSION_KEY);
        System.out.println("🗑️ Mall selection cleared from session");
    }
}