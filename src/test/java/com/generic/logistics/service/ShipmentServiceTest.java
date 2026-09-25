package com.generic.logistics.service;

import com.generic.logistics.config.LogisticsProperties;
import com.generic.logistics.dto.ShipmentRequest;
import com.generic.logistics.dto.StatusUpdateRequest;
import com.generic.logistics.event.ShipmentStatusEvent;
import com.generic.logistics.model.Role;
import com.generic.logistics.model.Shipment;
import com.generic.logistics.model.ShipmentStatus;
import com.generic.logistics.model.User;
import com.generic.logistics.repository.ShipmentRepository;
import com.generic.logistics.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private LogisticsProperties logisticsProperties;

    @InjectMocks private ShipmentService shipmentService;

    @Test
    void shouldCreateShipmentAndPublishEventSuccessfully() {
        ShipmentRequest request = new ShipmentRequest(10.0, 1L);
        User mockCustomer = new User();
        mockCustomer.setId(1L);
        mockCustomer.setEmail("customer@logistics.com");
        mockCustomer.setRole(Role.CUSTOMER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(logisticsProperties.baseRate()).thenReturn(BigDecimal.valueOf(5.50));
        when(logisticsProperties.ratePerKg()).thenReturn(BigDecimal.valueOf(2.25));

        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment shipment = invocation.getArgument(0);
            shipment.setId(100L);
            return shipment;
        });

        Shipment createdShipment = shipmentService.createShipment(request);

        assertThat(createdShipment).isNotNull();
        assertThat(createdShipment.getId()).isEqualTo(100L);
        assertThat(createdShipment.getTrackingNumber()).startsWith("TRK-");
        assertThat(createdShipment.getWeight()).isEqualTo(10.0);
        assertThat(createdShipment.getCurrentStatus()).isEqualTo(ShipmentStatus.ORDERED);
        assertThat(createdShipment.getShippingCost()).isEqualByComparingTo("28.00");

        verify(shipmentRepository).save(any(Shipment.class));
        verify(eventPublisher).publishEvent(any(ShipmentStatusEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringShipmentCreation() {
        ShipmentRequest request = new ShipmentRequest(5.0, 99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.createShipment(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Customer not found with ID: 99");

        verify(shipmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldGetShipmentByTrackingNumberSuccessfully() {
        String trackingNumber = "TRK-VALID-123";
        Shipment mockShipment = new Shipment();
        mockShipment.setTrackingNumber(trackingNumber);

        when(shipmentRepository.findByTrackingNumber(trackingNumber)).thenReturn(Optional.of(mockShipment));

        Shipment result = shipmentService.getShipmentByTrackingNumber(trackingNumber);

        assertThat(result).isNotNull();
        assertThat(result.getTrackingNumber()).isEqualTo(trackingNumber);
        verify(shipmentRepository).findByTrackingNumber(trackingNumber);
    }

    @Test
    void shouldThrowExceptionWhenShipmentNotFoundByTrackingNumber() {
        String invalidTrackingNumber = "TRK-NOT-FOUND";
        when(shipmentRepository.findByTrackingNumber(invalidTrackingNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.getShipmentByTrackingNumber(invalidTrackingNumber))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Shipment not found with tracking number: TRK-NOT-FOUND");

        verify(shipmentRepository).findByTrackingNumber(invalidTrackingNumber);
    }

    @Test
    void shouldUpdateShipmentStatusAndPublishEventSuccessfully() {
        com.generic.logistics.dto.StatusUpdateRequest request = new com.generic.logistics.dto.StatusUpdateRequest(
                ShipmentStatus.DELIVERED, "Delivered to reception"
        );

        User mockCustomer = new User();
        mockCustomer.setEmail("customer@logistics.com");

        User mockDriver = new User();
        mockDriver.setEmail("driver@logistics.com");
        mockDriver.setRole(Role.DRIVER);

        Shipment mockShipment = new Shipment();
        mockShipment.setId(1L);
        mockShipment.setCurrentStatus(ShipmentStatus.IN_TRANSIT);
        mockShipment.setCustomer(mockCustomer);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));
        when(userRepository.findByEmail("driver@logistics.com")).thenReturn(Optional.of(mockDriver));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(mockShipment);

        Shipment result = shipmentService.updateShipmentStatus(1L, request, "driver@logistics.com");

        assertThat(result).isNotNull();
        assertThat(result.getCurrentStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(result.getDriver()).isEqualTo(mockDriver);

        verify(shipmentRepository).save(any(Shipment.class));
        verify(eventPublisher).publishEvent(any(ShipmentStatusEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenShipmentNotFoundDuringStatusUpdate() {
        StatusUpdateRequest request = new StatusUpdateRequest(
                ShipmentStatus.DELIVERED, "Notes"
        );

        when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.updateShipmentStatus(99L, request, "driver@logistics.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Shipment not found with ID: 99");

        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenDriverNotFoundDuringStatusUpdate() {
        com.generic.logistics.dto.StatusUpdateRequest request = new com.generic.logistics.dto.StatusUpdateRequest(
                ShipmentStatus.DELIVERED, "Package delivered"
        );

        Shipment mockShipment = new Shipment();
        mockShipment.setId(1L);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));
        when(userRepository.findByEmail("unknown.driver@logistics.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.updateShipmentStatus(1L, request, "unknown.driver@logistics.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Driver not found with email: unknown.driver@logistics.com");

        verify(shipmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldUpdateShipmentStatusWithDefaultNotesWhenNotesAreNull() {
        StatusUpdateRequest request = new StatusUpdateRequest(
                ShipmentStatus.DELIVERED, null
        );

        User mockCustomer = new User();
        mockCustomer.setEmail("customer@logistics.com");

        User mockDriver = new User();
        mockDriver.setEmail("driver@logistics.com");

        Shipment mockShipment = new Shipment();
        mockShipment.setId(1L);
        mockShipment.setCurrentStatus(ShipmentStatus.IN_TRANSIT);
        mockShipment.setCustomer(mockCustomer);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(mockShipment));
        when(userRepository.findByEmail("driver@logistics.com")).thenReturn(Optional.of(mockDriver));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(mockShipment);

        Shipment result = shipmentService.updateShipmentStatus(1L, request, "driver@logistics.com");

        assertThat(result).isNotNull();
        verify(shipmentRepository).save(any(Shipment.class));
        verify(eventPublisher).publishEvent(argThat((ShipmentStatusEvent event) ->
                "Status updated by driver".equals(event.notes())
        ));
    }

}
