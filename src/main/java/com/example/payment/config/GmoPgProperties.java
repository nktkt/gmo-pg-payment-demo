package com.example.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gmopg")
public record GmoPgProperties(
        String siteId,
        String sitePass,
        String shopId,
        String shopPass,
        String apiBaseUrl,
        String tokenJsUrl
) {
}
