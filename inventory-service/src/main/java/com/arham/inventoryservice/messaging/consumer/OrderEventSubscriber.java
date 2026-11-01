package com.arham.inventoryservice.messaging.consumer;


import com.arham.inventoryservice.dto.OrderResponse;
import com.arham.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventSubscriber {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = "order-created"
    )
    public void consume(OrderResponse response) {
        inventoryService.handleOrderCreated(response);
    }
}