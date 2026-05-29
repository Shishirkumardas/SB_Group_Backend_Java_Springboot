package org.sb_ibms.services;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrentMallService {

    private static final String MALL_SESSION_KEY = "CURRENT_SHOPPING_MALL_ID";

    @Autowired
    private HttpSession httpSession;

    public void setCurrentMall(Long shoppingMallId) {
        httpSession.setAttribute(MALL_SESSION_KEY, shoppingMallId);
    }

    public Long getCurrentMallId() {
        return (Long) httpSession.getAttribute(MALL_SESSION_KEY);
    }

    public void clearCurrentMall() {
        httpSession.removeAttribute(MALL_SESSION_KEY);
    }
}
