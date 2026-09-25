package com.generic.logistics.listener;

import com.generic.logistics.event.ShipmentStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogisticsListener {

    @Async
    @EventListener
    public void handleShipmentStatusChange(ShipmentStatusEvent event) {
        log.info("Async Thread [{}]: Processing background tasks for shipment ID: {}",
                Thread.currentThread().getName(), event.shipmentId());
        try {
            Thread.sleep(2000);
            log.info("Async Thread [{}]: Task completed successfully for {}",
                    Thread.currentThread().getName(), event.customerEmail());
        } catch (InterruptedException e) {
            log.error("Async background process interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
