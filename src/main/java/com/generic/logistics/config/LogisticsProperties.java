package com.generic.logistics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.math.BigDecimal;

@ConfigurationProperties(prefix = "application.logistics.rates")
public record LogisticsProperties(
        BigDecimal baseRate,
        BigDecimal ratePerKg
) {}
