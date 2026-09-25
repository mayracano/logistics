package com.generic.logistics.controller;

import com.generic.logistics.dto.ShipmentRequest;
import com.generic.logistics.dto.StatusUpdateRequest;
import com.generic.logistics.model.Shipment;
import com.generic.logistics.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shipments", description = "Endpoints for managing and tracking commercial eCommerce shipments.")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new shipment", description = "Registers a shipment in the database and triggers async background notifications.")
    public ResponseEntity<EntityModel<Shipment>> createShipment(@Valid @RequestBody ShipmentRequest request) {
        log.info("REST hit: Request to create new shipment order");
        Shipment createdShipment = shipmentService.createShipment(request);

        EntityModel<Shipment> resource = EntityModel.of(createdShipment);

        Link selfLink = linkTo(methodOn(ShipmentController.class).getShipmentByTrackingNumber(createdShipment.getTrackingNumber())).withSelfRel();
        resource.add(selfLink);

        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }

    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Track shipment by tracking number", description = "Public endpoint allowing anyone to view the live status of an order.")
    public ResponseEntity<EntityModel<Shipment>> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        log.info("REST hit: Tracking request for number {}", trackingNumber);

        Shipment shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);

        EntityModel<Shipment> resource = EntityModel.of(shipment);
        Link selfLink = linkTo(methodOn(ShipmentController.class).getShipmentByTrackingNumber(trackingNumber)).withSelfRel();
        resource.add(selfLink);

        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('DRIVER')")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update shipment status by driver")
    public ResponseEntity<org.springframework.hateoas.EntityModel<Shipment>> updateShipmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            java.security.Principal principal) {

        log.info("REST hit: Request from driver to update status of shipment {}", id);
        Shipment updatedShipment = shipmentService.updateShipmentStatus(id, request, principal.getName());

        org.springframework.hateoas.EntityModel<Shipment> resource = org.springframework.hateoas.EntityModel.of(updatedShipment);
        org.springframework.hateoas.Link selfLink = linkTo(methodOn(ShipmentController.class).getShipmentByTrackingNumber(updatedShipment.getTrackingNumber())).withSelfRel();
        resource.add(selfLink);

        return ResponseEntity.ok(resource);
    }
}
