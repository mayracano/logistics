package com.generic.logistics.service;

import com.generic.logistics.config.LogisticsProperties;
import com.generic.logistics.dto.ShipmentRequest;
import com.generic.logistics.dto.StatusUpdateRequest;
import com.generic.logistics.event.ShipmentStatusEvent;
import com.generic.logistics.model.Shipment;
import com.generic.logistics.model.ShipmentStatus;
import com.generic.logistics.model.User;
import com.generic.logistics.repository.ShipmentRepository;
import com.generic.logistics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LogisticsProperties logisticsProperties;

    @Transactional
    public Shipment createShipment(ShipmentRequest request) {
        log.info("Creating new package order for customer ID: {}", request.customerId());

        User customer = userRepository.findById(request.customerId())
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with ID: " + request.customerId()));

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        shipment.setWeight(request.weight());
        shipment.setShippingCost(calculateShippingCost(request.weight()));
        shipment.setCurrentStatus(ShipmentStatus.ORDERED);
        shipment.setCustomer(customer);

        Shipment savedShipment = shipmentRepository.save(shipment);

        eventPublisher.publishEvent(new ShipmentStatusEvent(
                savedShipment.getId(),
                savedShipment.getCurrentStatus().name(),
                customer.getEmail(),
                "Package registration order created"
        ));

        return savedShipment;
    }

    private BigDecimal calculateShippingCost(Double weight) {
        BigDecimal packageWeight = BigDecimal.valueOf(weight);
        return logisticsProperties.baseRate().add(packageWeight.multiply(logisticsProperties.ratePerKg()));
    }

    @Transactional(readOnly = true)
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {
        log.info("Searching database for tracking number: {}", trackingNumber);
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Shipment not found with tracking number: " + trackingNumber));
    }

    @Transactional
    public Shipment updateShipmentStatus(Long id, StatusUpdateRequest request, String driverEmail) {
        log.info("Driver {} updating shipment ID {} to status {}", driverEmail, id, request.status());

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Shipment not found with ID: " + id));

        User driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Driver not found with email: " + driverEmail));

        shipment.setCurrentStatus(request.status());
        shipment.setDriver(driver);

        Shipment updatedShipment = shipmentRepository.save(shipment);

        eventPublisher.publishEvent(new ShipmentStatusEvent(
                updatedShipment.getId(),
                updatedShipment.getCurrentStatus().name(),
                shipment.getCustomer().getEmail(),
                request.notes() != null ? request.notes() : "Status updated by driver"
        ));

        return updatedShipment;
    }
}
