package com.arham.orderservice.service.impl;

import com.arham.orderservice.dto.OrderItemRequest;
import com.arham.orderservice.dto.OrderRequest;
import com.arham.orderservice.dto.OrderResponse;
import com.arham.orderservice.dto.PageResponse;
import com.arham.orderservice.dto.ProductResponse;
import com.arham.orderservice.entity.Order;
import com.arham.orderservice.entity.OrderItem;
import com.arham.orderservice.entity.OrderStatus;
import com.arham.orderservice.exception.DuplicateResourceException;
import com.arham.orderservice.exception.ResourceNotFoundException;
import com.arham.orderservice.gateway.ProductGateway;
import com.arham.orderservice.mapper.OrderMapper;
import com.arham.orderservice.messaging.producer.OrderEventPublisher;
import com.arham.orderservice.repository.OrderRepository;
import com.arham.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductGateway productGateway;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("Received request to create order for customer ID: {}", request.getCustomerId());

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        String orderNumber;
        if (request.getOrderNumber() != null && !request.getOrderNumber().isBlank()) {
            if (orderRepository.existsByOrderNumber(request.getOrderNumber())) {
                throw new DuplicateResourceException("Order", "orderNumber", request.getOrderNumber());
            }
            orderNumber = request.getOrderNumber();
        } else {
            orderNumber = "ORD-" + UUID.randomUUID();
        }

        // 1. Collect unique SKU codes from order items
        List<String> skuCodes = request.getOrderItems().stream()
                .map(OrderItemRequest::getSkuCode)
                .filter(sku -> sku != null && !sku.isBlank())
                .distinct()
                .toList();

        if (skuCodes.isEmpty()) {
            throw new IllegalArgumentException("Order items must contain valid SKU codes");
        }

        // 2. Fetch and validate products via Product Gateway (encapsulating Resilience4j & OpenFeign)
        log.info("Fetching and validating {} products from product gateway for order: {}", skuCodes.size(), orderNumber);
        Map<String, ProductResponse> productMap = productGateway.fetchAndValidateProducts(skuCodes);

        // 3. Build order and order items with validated data and authoritative prices
        Order order = orderMapper.toEntity(request);
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getOrderItems()) {
            ProductResponse product = productMap.get(itemRequest.getSkuCode());
            if (product == null) {
                throw new ResourceNotFoundException("Product", "skuCode", itemRequest.getSkuCode());
            }

            OrderItem item = orderMapper.toItemEntity(itemRequest);

            // Best Practice: Use authoritative catalog price from product-service
            BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : itemRequest.getPrice();
            item.setPrice(unitPrice);

            order.addOrderItem(item);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);
        }
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);



        log.info("Order created successfully with ID: {}, orderNumber: {}, status: {}, totalAmount: {}",
                savedOrder.getId(), savedOrder.getOrderNumber(), savedOrder.getStatus(), savedOrder.getTotalAmount());


        //  create Kafka event and other will consume that (inventory, Notification)
        OrderResponse response = orderMapper.toResponse(savedOrder);
        orderEventPublisher.publish(response);


        return response;
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
