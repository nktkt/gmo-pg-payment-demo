package com.example.payment.config;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

public class LogMaskingPatternLayout extends PatternLayout {

    private static final Pattern CARD_NUMBER_PATTERN =
            Pattern.compile("\\b(\\d{4})[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?(\\d{4})\\b");

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("(?i)(Token=)([^&\\s]+)");

    private static final Pattern ACCESS_PASS_PATTERN =
            Pattern.compile("(?i)(AccessPass=)([^&\\s]+)");

    private static final Pattern SHOP_PASS_PATTERN =
            Pattern.compile("(?i)(ShopPass=)([^&\\s]+)");

    private static final Pattern SECURITY_CODE_PATTERN =
            Pattern.compile("(?i)(SecurityCode=)\\d{3,4}");

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        return maskSensitiveData(message);
    }

    private String maskSensitiveData(String message) {
        message = CARD_NUMBER_PATTERN.matcher(message).replaceAll("****-****-****-$2");
        message = TOKEN_PATTERN.matcher(message).replaceAll("$1****");
        message = ACCESS_PASS_PATTERN.matcher(message).replaceAll("$1****");
        message = SHOP_PASS_PATTERN.matcher(message).replaceAll("$1****");
        message = SECURITY_CODE_PATTERN.matcher(message).replaceAll("$1****");
        return message;
    }
}
