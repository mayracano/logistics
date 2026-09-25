package com.generic.logistics.repository;

import com.generic.logistics.model.Shipment;
import com.generic.logistics.model.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {
     Optional<Shipment> findByTrackingNumber(String trackingNumber);
     List<Shipment> findByDriverIdAndCurrentStatus(
             Long driver_id, @NotNull(message = "current status is required")
             ShipmentStatus currentStatus);
}
