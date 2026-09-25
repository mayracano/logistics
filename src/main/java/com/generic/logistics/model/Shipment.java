package com.generic.logistics.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Data
@Table(name="shipments")
@Entity
@Audited
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="tracking_number", nullable=false, unique = true, length = 50)
    @NotNull(message = "tracking number is required")
    private String trackingNumber;

    @Column(name="weight", nullable = false)
    @NotNull(message = "weight is required")
    @Min(value = 0, message = "Weight must be greater than 0")
    private Double weight;

    @Column(name="status",  nullable = false, precision =  10, scale = 2)
    @NotNull(message = "current status is required")
    @Enumerated(EnumType.STRING)
    private ShipmentStatus currentStatus;

    @Column(name="shipping_cost")
    @NotNull(message = "shipping cost is required")
    private BigDecimal shippingCost;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = true)
    private User driver;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
}
