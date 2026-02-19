package com.example.calculator;

import java.math.BigDecimal;

final class NumberFormatUtil {
    private NumberFormatUtil() {
    }

    static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        String text = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        return "-0".equals(text) ? "0" : text;
    }
}
