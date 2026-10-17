package com.arham.orderservice.service.impl;

import com.arham.orderservice.dto.OrderItemRequest;
import com.arham.orderservice.dto.OrderRequest;
import com.arham.orderservice.dto.OrderResponse;
import com.arham.orderservice.dto.PageResponse;
import com.arham.orderservice.entity.Order;
import com.arham.orderservice.entity.OrderItem;
import com.arham.orderservice.entity.OrderStatus;
import com.arham.orderservice.exception.DuplicateResourceException;
import com.arham.orderservice.exception.ResourceNotFoundException;
import com.arham.orderservice.mapper.OrderMapper;
import com.arham.orderservice.repository.OrderRepository;
import com.arham.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Received request to create order for customer ID: {}", request.getCustomerId());

        String orderNumber;
        if (request.getOrderNumber() != null && !request.getOrderNumber().isBlank()) {
            if (orderRepository.existsByOrderNumber(request.getOrderNumber())) {
                throw new DuplicateResourceException("Order", "orderNumber", request.getOrderNumber());
            }
            orderNumber = request.getOrderNumber();
        } else {
            orderNumber = "ORD-" + UUID.randomUUID();
        }

        Order order = orderMapper.toEntity(request);
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (request.getOrderItems() != null) {
            for (OrderItemRequest itemRequest : request.getOrderItems()) {
                OrderItem item = orderMapper.toItemEntity(itemRequest);
                order.addOrderItem(item);
                BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(lineTotal);
            }
        }
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}, orderNumber: {}, status: {}, totalAmount: {}",
                savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getStatus(), savedOrder.getTotalAmount());

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order with orderNumber: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return orderMapper.toResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {}, page: {}", customerId, pageable.getPageNumber());
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        return PageResponse.from(orderPage.map(orderMapper::toResponse));
    }

    @Override
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with pagination: {}", pageable);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        return PageResponse.from(orderPage.map(orderMapper::toResponse));
    }
}
