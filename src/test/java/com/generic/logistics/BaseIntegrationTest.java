package com.generic.logistics;

import com.generic.logistics.config.SecurityConfig;
import com.generic.logistics.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;

@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.properties.hibernate.default_schema=logistics",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.sql.init.mode=always"
        })
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    protected WebTestClient webTestClient;
    @Autowired
    protected JwtService jwtService;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("postgres")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String jdbUrl = postgres.getJdbcUrl();
        String delimiter = jdbUrl.contains("?") ? "&" : "?";
        String finalUrl = jdbUrl + delimiter + "currentSchema=logistics";

        registry.add("spring.datasource.url", () -> finalUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("application.security.jwt.secret-key", () -> "TXV5SW1wb3J0YW50ZUNsYXZlU2VjcmV0YVBhcmFTcHJpbmdCb290TmV2ZWwyU2VtaVNlbmlvcg==");
        registry.add("application.security.jwt.expiration", () -> 86400000L);
        registry.add("springdoc.api-docs.enabled", () -> "false");
    }

    @BeforeEach
    protected void setUpBase() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }
}