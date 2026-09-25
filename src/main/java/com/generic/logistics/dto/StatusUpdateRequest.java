package com.generic.logistics.dto;

import com.generic.logistics.model.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "New status is required")
        ShipmentStatus status,

        String notes
) {
}
