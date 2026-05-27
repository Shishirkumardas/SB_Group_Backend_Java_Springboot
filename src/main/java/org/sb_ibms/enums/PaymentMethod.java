package org.sb_ibms.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum PaymentMethod {
    BKASH,
    NAGAD,
    ROCKET,
    CASH,
    CARD,
    BANK;

    public static List<String> getAllMethods() {
        return Arrays.stream(PaymentMethod.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
