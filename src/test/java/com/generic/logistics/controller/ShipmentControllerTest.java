package com.generic.logistics.controller;

import com.generic.logistics.service.JwtService;
import com.generic.logistics.dto.ShipmentRequest;
import com.generic.logistics.model.Shipment;
import com.generic.logistics.model.ShipmentStatus;
import com.generic.logistics.service.ShipmentService;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentService shipmentService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnCreatedAndHateoasLinksWhenAdminCreatesShipment() throws Exception {
        ShipmentRequest request = new ShipmentRequest(10.0, 1L);
        Shipment mockShipment = new Shipment();
        mockShipment.setId(42L);
        mockShipment.setShippingCost(new BigDecimal("28.00"));
        mockShipment.setTrackingNumber("TRK-12345678");
        mockShipment.setWeight(10.0);
        mockShipment.setCurrentStatus(ShipmentStatus.ORDERED);

        when(shipmentService.createShipment(any(ShipmentRequest.class))).thenReturn(mockShipment);

        mockMvc.perform(post("/api/shipments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.trackingNumber").value("TRK-12345678"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnOkWhenTrackingShipmentByNumber() throws Exception {
        String trackingNumber = "TRK-12345678";
        Shipment mockShipment = new Shipment();
        mockShipment.setId(42L);
        mockShipment.setTrackingNumber(trackingNumber);
        mockShipment.setCurrentStatus(ShipmentStatus.ORDERED);

        when(shipmentService.getShipmentByTrackingNumber(trackingNumber)).thenReturn(mockShipment);

        mockMvc.perform(get("/api/shipments/track/{trackingNumber}", trackingNumber)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value(trackingNumber))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @WithMockUser(roles = "DRIVER", username = "driver@logistics.com")
    void shouldReturnOkWhenDriverUpdatesShipmentStatus() throws Exception {
        com.generic.logistics.dto.StatusUpdateRequest request = new com.generic.logistics.dto.StatusUpdateRequest(
                ShipmentStatus.OUT_FOR_DELIVERY, "In route"
        );

        Shipment mockShipment = new Shipment();
        mockShipment.setId(1L);
        mockShipment.setTrackingNumber("TRK-11223344");
        mockShipment.setCurrentStatus(ShipmentStatus.OUT_FOR_DELIVERY);

        when(shipmentService.updateShipmentStatus(any(Long.class), any(com.generic.logistics.dto.StatusUpdateRequest.class), any(String.class)))
                .thenReturn(mockShipment);

        mockMvc.perform(put("/api/shipments/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value(ShipmentStatus.OUT_FOR_DELIVERY.name()))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

}
