package com.generic.logistics.config;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class LogisticsPropertiesTest {

    @Test
    void shouldStoreAndRetrievePropertiesSuccessfully() {
        BigDecimal base = BigDecimal.valueOf(5.50);
        BigDecimal perKg = BigDecimal.valueOf(2.25);

        LogisticsProperties properties = new LogisticsProperties(base, perKg);

        assertThat(properties.baseRate()).isEqualTo(base);
        assertThat(properties.ratePerKg()).isEqualTo(perKg);
    }
}
