package com.generic.logistics;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

class LogisticsApplicationTests {
    @Test
    void mainStartsSpringApplication() {
        String[] args = {"--spring.main.web-application-type=none"};

        try (MockedStatic<org.springframework.boot.SpringApplication> mocked = mockStatic(org.springframework.boot.SpringApplication.class)) {
            LogisticsApplication.main(args);

            mocked.verify(() -> org.springframework.boot.SpringApplication.run(LogisticsApplication.class, args));
        }
    }
}
