package com.arham.notificationservice.messaging.consumer;

import com.arham.notificationservice.dto.OrderResponse;
import com.arham.notificationservice.dto.ProductResponse;
import com.arham.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class OrderEventSubscriber {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order-created"
    )
    public void consume(OrderResponse response) {
        notificationService.handleOrderCreated(response);
    }
}