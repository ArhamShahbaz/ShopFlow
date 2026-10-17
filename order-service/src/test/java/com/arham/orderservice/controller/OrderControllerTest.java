package com.arham.orderservice.controller;

import com.arham.orderservice.dto.ApiResponse;
import com.arham.orderservice.dto.OrderItemRequest;
import com.arham.orderservice.dto.OrderItemResponse;
import com.arham.orderservice.dto.OrderRequest;
import com.arham.orderservice.dto.OrderResponse;
import com.arham.orderservice.entity.OrderStatus;
import com.arham.orderservice.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @Test
    @DisplayName("POST /api/v1/orders should call service and return 201 Created with OrderResponse")
    void shouldCreateOrder() {
        // Given
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .skuCode("IPHONE-15-PRO")
                .quantity(1)
                .price(new BigDecimal("999.99"))
                .build();

        OrderRequest request = OrderRequest.builder()
                .customerId(101L)
                .orderItems(List.of(itemRequest))
                .build();

        OrderItemResponse itemResponse = OrderItemResponse.builder()
                .id(1L)
                .skuCode("IPHONE-15-PRO")
                .quantity(1)
                .price(new BigDecimal("999.99"))
                .build();

        OrderResponse responseDto = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-12345")
                .customerId(101L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("999.99"))
                .orderItems(List.of(itemResponse))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(responseDto);

        // When
        ResponseEntity<ApiResponse<OrderResponse>> responseEntity = orderController.createOrder(request);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isTrue();
        assertThat(responseEntity.getBody().getMessage()).isEqualTo("Order created successfully");
        assertThat(responseEntity.getBody().getData().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(responseEntity.getBody().getData().getOrderNumber()).isEqualTo("ORD-12345");
        assertThat(responseEntity.getBody().getData().getTotalAmount()).isEqualByComparingTo(new BigDecimal("999.99"));

        verify(orderService).createOrder(request);
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} should return 200 OK with order")
    void shouldGetOrderById() {
        OrderResponse responseDto = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-12345")
                .customerId(101L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("999.99"))
                .build();

        when(orderService.getOrderById(1L)).thenReturn(responseDto);

        ResponseEntity<ApiResponse<OrderResponse>> responseEntity = orderController.getOrderById(1L);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isTrue();
        assertThat(responseEntity.getBody().getData().getOrderNumber()).isEqualTo("ORD-12345");

        verify(orderService).getOrderById(1L);
    }
}
