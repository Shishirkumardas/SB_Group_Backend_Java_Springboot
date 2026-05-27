package org.sb_ibms.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ShoppingMallPaymentMethod {
    CASH,
    BKASH,
    NAGAD,
    ROCKET,
    CARD,
    BANK;

    public static List<String> getAllMethods() {
        return Arrays.stream(ShoppingMallPaymentMethod.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
