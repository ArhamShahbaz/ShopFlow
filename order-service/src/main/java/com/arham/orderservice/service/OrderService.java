package com.arham.orderservice.service;

import com.arham.orderservice.dto.OrderRequest;
import com.arham.orderservice.dto.OrderResponse;
import com.arham.orderservice.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByOrderNumber(String orderNumber);

    PageResponse<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);

    PageResponse<OrderResponse> getAllOrders(Pageable pageable);
}
