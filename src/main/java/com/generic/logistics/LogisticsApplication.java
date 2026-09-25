package com.generic.logistics;

import com.generic.logistics.config.JwtProperties;
import com.generic.logistics.config.LogisticsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hateoas.autoconfigure.HypermediaAutoConfiguration;

@SpringBootApplication(exclude = {HypermediaAutoConfiguration.class})
public class LogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticsApplication.class, args);
    }

}
