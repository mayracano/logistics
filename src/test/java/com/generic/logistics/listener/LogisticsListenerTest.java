package com.generic.logistics.listener;

import com.generic.logistics.event.ShipmentStatusEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class LogisticsListenerTest {

    @InjectMocks
    private LogisticsListener logisticsListener;

    @Test
    void shouldHandleShipmentStatusChangeEventSuccessfully() {
        ShipmentStatusEvent event = new ShipmentStatusEvent(
                42L,
                "ORDERED",
                "customer@email.com",
                "Shipment successfully processed"
        );

        assertThatCode(() -> logisticsListener.handleShipmentStatusChange(event))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldHandleInterruptedExceptionWhenThreadIsInterrupted() throws InterruptedException {
        ShipmentStatusEvent event = new ShipmentStatusEvent(
                42L,
                "ORDERED",
                "customer@email.com",
                "Interruption test"
        );

        Thread testThread = new Thread(() -> logisticsListener.handleShipmentStatusChange(event));

        testThread.start();
        Thread.sleep(200);
        testThread.interrupt();
        testThread.join(1000);
    }
}
