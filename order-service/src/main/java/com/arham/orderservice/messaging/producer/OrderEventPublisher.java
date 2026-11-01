package com.arham.orderservice.messaging.producer;

import com.arham.orderservice.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderResponse> kafkaTemplate;

    public CompletableFuture<SendResult<String, OrderResponse>> publish(OrderResponse response) {

        return kafkaTemplate.send(
                "order-created",
                response.getOrderNumber(),
                response
        );
    }
}