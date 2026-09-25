package com.generic.logistics.controller;

import com.generic.logistics.BaseIntegrationTest;
import com.generic.logistics.dto.ShipmentRequest;
import com.generic.logistics.model.Role;
import com.generic.logistics.model.Shipment;
import com.generic.logistics.model.ShipmentStatus;
import com.generic.logistics.model.User;
import com.generic.logistics.repository.ShipmentRepository;
import com.generic.logistics.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ShipmentControllerIT extends BaseIntegrationTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedCustomer;

    private String adminToken;

    @BeforeEach
    void setUp() {
        super.setUpBase();

        User adminUser = new User();
        adminUser.setFirstName("Logistics");
        adminUser.setLastName("Admin");
        adminUser.setEmail("admin@logistics.com");
        adminUser.setPassword(passwordEncoder.encode("securePassword123"));
        adminUser.setRole(Role.ADMIN);
        adminUser.setPhone("123 456 7890");
        userRepository.save(adminUser);

        adminToken = jwtService.generateToken(
                Map.of("role", Role.ADMIN.name()),
                "admin@logistics.com"
        );

        User customerUser = new User();
        customerUser.setFirstName("John");
        customerUser.setLastName("Doe");
        customerUser.setEmail("john.doe@email.com");
        customerUser.setPassword(passwordEncoder.encode("customerPass123"));
        customerUser.setRole(Role.CUSTOMER);
        customerUser.setPhone("123 456 7890");
        this.savedCustomer = userRepository.save(customerUser);
    }

    @AfterEach
    void tearDown() {
        shipmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateShipmentSuccessfullyWithHateoasLinks() {
        ShipmentRequest request = new ShipmentRequest(12.5, savedCustomer.getId());

        webTestClient.post()
                .uri("/api/shipments")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.trackingNumber").exists()
                .jsonPath("$.weight").isEqualTo(12.5)
                .jsonPath("$.currentStatus").isEqualTo(ShipmentStatus.ORDERED.name())
                .jsonPath("$.links[0].rel").isEqualTo("self")
                .jsonPath("$.links[0].href").exists();

        assertThat(shipmentRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldTrackShipmentSuccessfullyByTrackingNumber() {

        User driverUser = new User();
        driverUser.setFirstName("Michael");
        driverUser.setLastName("Schumacher");
        driverUser.setEmail("driver.test@logistics.com");
        driverUser.setPassword(passwordEncoder.encode("driverPass123"));
        driverUser.setRole(Role.DRIVER);
        driverUser.setPhone("123 456 7890");
        User savedDriver = userRepository.save(driverUser);

        Shipment existingShipment = new Shipment();
        existingShipment.setTrackingNumber("TRK-TEST-99");
        existingShipment.setWeight(4.5);
        existingShipment.setShippingCost(BigDecimal.valueOf(15.60));
        existingShipment.setCurrentStatus(ShipmentStatus.IN_TRANSIT);
        existingShipment.setCustomer(savedCustomer);
        existingShipment.setDriver(savedDriver);
        shipmentRepository.save(existingShipment);

        webTestClient.get()
                .uri("/api/shipments/track/TRK-TEST-99")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.trackingNumber").isEqualTo("TRK-TEST-99")
                .jsonPath("$.currentStatus").isEqualTo(ShipmentStatus.IN_TRANSIT.name())
                .jsonPath("$.links[0].rel").isEqualTo("self")
                .jsonPath("$.links[0].href").exists();
    }

    @Test
    void shouldReturnNotFoundWhenTrackingNumberDoesNotExist() {
        webTestClient.get()
                .uri("/api/shipments/track/TRK-INVALID-00")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Not Found")
                .jsonPath("$.message").isEqualTo("Shipment not found with tracking number: TRK-INVALID-00");
    }

    @Test
    void shouldDenyShipmentCreationWhenUserIsNotAdmin() {
        String customerToken = jwtService.generateToken(
                Map.of("role", Role.CUSTOMER.name()),
                "john.doe@email.com"
        );

        ShipmentRequest request = new ShipmentRequest(5.0, savedCustomer.getId());

        webTestClient.post()
                .uri("/api/shipments")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldAllowDriverToUpdateShipmentStatusSuccessfully() {
        User driverUser = new User();
        driverUser.setFirstName("Alex");
        driverUser.setLastName("Delivery");
        driverUser.setEmail("alex.driver@logistics.com");
        driverUser.setPassword(passwordEncoder.encode("driverPass123"));
        driverUser.setPhone("0987654321");
        driverUser.setRole(Role.DRIVER);
        User savedDriver = userRepository.save(driverUser);

        String driverToken = jwtService.generateToken(
                Map.of("role", Role.DRIVER.name()),
                "alex.driver@logistics.com"
        );

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("TRK-UPD-01");
        shipment.setWeight(5.0);
        shipment.setShippingCost(BigDecimal.valueOf(16.75));
        shipment.setCurrentStatus(ShipmentStatus.ORDERED);
        shipment.setCustomer(savedCustomer);
        Shipment savedShipment = shipmentRepository.save(shipment);

        com.generic.logistics.dto.StatusUpdateRequest request = new com.generic.logistics.dto.StatusUpdateRequest(
                ShipmentStatus.DELIVERED, "Left at front door"
        );

        webTestClient.put()
                .uri("/api/shipments/" + savedShipment.getId() + "/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + driverToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.currentStatus").isEqualTo(ShipmentStatus.DELIVERED.name())
                .jsonPath("$.links[0].rel").isEqualTo("self")
                .jsonPath("$.links[0].href").exists();

        Shipment updatedShipment = shipmentRepository.findById(savedShipment.getId()).orElseThrow();
        assertThat(updatedShipment.getCurrentStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(updatedShipment.getDriver().getId()).isEqualTo(savedDriver.getId());
    }

}
