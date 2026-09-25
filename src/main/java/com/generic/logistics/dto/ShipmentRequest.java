package com.generic.logistics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShipmentRequest(
        @NotNull(message="weight is required")
        @Min(value=0, message = "Value must be higher than 0")
        Double weight,
        @NotNull(message = "customer id is required")
        Long customerId) {
}
