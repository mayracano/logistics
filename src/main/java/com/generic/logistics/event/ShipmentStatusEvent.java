package com.generic.logistics.event;

public record ShipmentStatusEvent(
        Long shipmentId,
        String status,
        String customerEmail,
        String notes
) {}
