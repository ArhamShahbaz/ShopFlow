package com.arham.orderservice.service;

import com.arham.orderservice.dto.OrderItemRequest;
import com.arham.orderservice.dto.OrderRequest;
import com.arham.orderservice.dto.OrderResponse;
import com.arham.orderservice.entity.Order;
import com.arham.orderservice.entity.OrderItem;
import com.arham.orderservice.entity.OrderStatus;
import com.arham.orderservice.exception.DuplicateResourceException;
import com.arham.orderservice.exception.ResourceNotFoundException;
import com.arham.orderservice.mapper.OrderMapper;
import com.arham.orderservice.repository.OrderRepository;
import com.arham.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderMapper orderMapper;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderMapper = Mappers.getMapper(OrderMapper.class);
        orderService = new OrderServiceImpl(orderRepository, orderMapper);
    }

    @Test
    @DisplayName("Should successfully create an order with status PENDING and calculated total amount")
    void shouldCreateOrderWithStatusPending() {
        // Given
        OrderItemRequest item1 = OrderItemRequest.builder()
                .skuCode("IPHONE-15-PRO")
                .quantity(2)
                .price(new BigDecimal("999.99"))
                .build();

        OrderItemRequest item2 = OrderItemRequest.builder()
                .skuCode("AIRPODS-PRO-2")
                .quantity(1)
                .price(new BigDecimal("249.00"))
                .build();

        OrderRequest request = OrderRequest.builder()
                .customerId(101L)
                .orderItems(List.of(item1, item2))
                .build();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getCustomerId()).isEqualTo(101L);
        assertThat(savedOrder.getOrderNumber()).isNotNull().startsWith("ORD-");
        // 2 * 999.99 = 1999.98; 1 * 249.00 = 249.00; total = 2248.98
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2248.98"));
        assertThat(savedOrder.getOrderItems()).hasSize(2);
        assertThat(savedOrder.getOrderItems().get(0).getOrder()).isEqualTo(savedOrder);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2248.98"));
        assertThat(response.getOrderItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException if custom order number already exists")
    void shouldThrowDuplicateResourceExceptionWhenOrderNumberExists() {
        // Given
        OrderRequest request = OrderRequest.builder()
                .orderNumber("ORD-CUSTOM-001")
                .customerId(101L)
                .orderItems(List.of(
                        OrderItemRequest.builder()
                                .skuCode("MACBOOK-PRO-16")
                                .quantity(1)
                                .price(new BigDecimal("2499.00"))
                                .build()
                ))
                .build();

        when(orderRepository.existsByOrderNumber("ORD-CUSTOM-001")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ORD-CUSTOM-001");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should fetch order by ID successfully")
    void shouldGetOrderById() {
        // Given
        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-123")
                .customerId(50L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        OrderResponse response = orderService.getOrderById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderNumber()).isEqualTo("ORD-123");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order does not exist")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
